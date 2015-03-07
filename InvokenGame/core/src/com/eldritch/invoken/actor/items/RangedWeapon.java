package com.eldritch.invoken.actor.items;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.actor.type.Agent.Direction;
import com.eldritch.invoken.actor.type.Agent.RayTarget;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.HandledBullet;
import com.eldritch.invoken.actor.type.HandledProjectile;
import com.eldritch.invoken.actor.Inventory;
import com.eldritch.invoken.effects.Bleed;
import com.eldritch.invoken.effects.Stunned;
import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.proto.Effects.DamageType;
import com.eldritch.invoken.proto.Items.Item.DamageMod;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.util.Settings;
import com.google.common.base.Strings;

public class RangedWeapon extends Item {
    private static final float DAMAGE_SCALE = 1;
    private static final float BULLET_VELOCITY = 25;
    private static final float COOLDOWN = 0.5f;
    
	private final Map<Direction, Animation> animations = new HashMap<Direction, Animation>();
	private final TextureRegion texture;
	private final DamageType primary;
	
	public RangedWeapon(com.eldritch.invoken.proto.Items.Item item) {
		this(item, getRegion(item.getAsset()));
	}
	
	public RangedWeapon(com.eldritch.invoken.proto.Items.Item item, TextureRegion texture) {
        super(item, texture);
        this.texture = texture;
        
        // get primary damage type
        DamageMod greatest = null;
        for (DamageMod mod : getData().getDamageModifierList()) {
            if (greatest == null || mod.getMagnitude() > greatest.getMagnitude()) {
                greatest = mod;
            }
        }
        primary = greatest != null ? greatest.getDamage() : DamageType.PHYSICAL;
    }
	
	public void render(Vector2 position, Vector2 direction, OrthogonalTiledMapRenderer renderer) {
	    if (texture == null) {
	        return;
	    }
	    
        float width = getWidth();
        float height = getHeight();
        
        // offset along the x-axis; we use this because longer guns require more support to hold
        // properly
        float dx = width / 4;
        
        float theta = direction.angle();
        boolean flipY = theta > 90 && theta < 270;
        
        Batch batch = renderer.getBatch();
        batch.begin();
        batch.draw(texture.getTexture(),
                position.x - width / 2 - dx, position.y - height / 2,  // position
                width / 2 + dx, height / 2,  // origin
                width, height,  // size
                1f, 1f,  // scale
                theta,  // rotation
                0, 0,  // srcX, srcY
                texture.getRegionWidth(), texture.getRegionHeight(),  // srcWidth, srcHeight
                false, flipY);  // flipX, flipY
        batch.end();
	}
	
	public HandledProjectile getProjectile(Agent owner) {
	    switch (primary) {
	        case THERMAL:
                return new RangedWeaponBullet(owner);
            default:
                return new RangedWeaponRay(owner);
	    }
	}
	
	public float getDamage() {
	    float damage = 0;
	    for (DamageMod mod : getData().getDamageModifierList()) {
	        damage += mod.getMagnitude();
	    }
	    return damage;
	}
	
	public float getCooldown() {
	    return COOLDOWN;
	}
	
	@Override
    public boolean isEquipped(Inventory inventory) {
        return inventory.getRangedWeapon() == this;
    }
	
	@Override
	public void equipFrom(Inventory inventory) {
		inventory.setRangedWeapon(this);
	}
	
	@Override
	public void unequipFrom(Inventory inventory) {
		if (inventory.getRangedWeapon() == this) {
			inventory.setRangedWeapon(null);
		}
	}
	
	@Override
	protected Animation getAnimation(Activity activity, Direction direction) {
		return animations.get(direction);
	}
	
	@Override
    public String toString() {
        StringBuilder result = new StringBuilder(String.format("%s\n"
                + "Range: %.2f\n",
                super.toString(), data.getRange()));
        result.append("Damage:");
        for (DamageMod mod : data.getDamageModifierList()) {
            result.append(String.format("\n  %s: %d", mod.getDamage(), mod.getMagnitude()));
        }
        return result.toString();
    }
	   
    private static TextureRegion getRegion(String asset) {
        if (!Strings.isNullOrEmpty(asset)) {
            return new TextureRegion(GameScreen.getTexture(getAssetPath(asset)));
        } else {
            return null;
        }
    }
    
    private static String getAssetPath(String asset) {
        return String.format("sprite/items/weapons/%s.png", asset);
    }
    
    public static class RangedWeaponBullet extends HandledBullet {
        private static final TextureRegion texture = new TextureRegion(
                GameScreen.getTexture("sprite/effects/bullet1.png"));

        public RangedWeaponBullet(Agent owner) {
            super(owner, texture, BULLET_VELOCITY, DAMAGE_SCALE);
        }

        @Override
        protected void apply(Agent owner, Agent target) {
            float magnitude = getDamage(target)
                    * owner.getInventory().getRangedWeapon().getDamage();
            target.applyForce(velocity.cpy().nor().scl(100));
            target.addEffect(new Stunned(owner, target, 0.2f));
            target.addEffect(new Bleed(owner, target, magnitude));
        }

        @Override
        protected TextureRegion getTexture(float stateTime) {
            return texture;
        }
    }

    public static class RangedWeaponRay implements HandledProjectile {
        private static final float FLASH_SECS = 0.075f;
        private static final TextureRegion texture = new TextureRegion(
                GameScreen.getTexture("sprite/effects/bullet2.png"));

        private final Vector2 position = new Vector2();
        private final Vector2 direction = new Vector2();

        private final Agent owner;
        private final boolean reflected;
        private RayTarget target = null;
        private float elapsed = 0;

        private final float height = 0.1f;
        private float width = 0;
        
        private RangedWeaponRay successor = null;

        public RangedWeaponRay(Agent owner) {
            this(owner, owner.getWeaponSentry().getPosition(), owner.getWeaponSentry()
                    .getDirection(), false);
        }

        public RangedWeaponRay(Agent owner, Vector2 position, Vector2 direction, boolean reflected) {
            this.owner = owner;
            this.reflected = reflected;

            // the owner may move after firing, but this vapor trail should not
            this.position.set(position);
            this.direction.set(direction);
        }

        @Override
        public void update(float delta, Location location) {
            if (target == null) {
                apply();
            } else {
                elapsed += delta;
            }
            
            if (successor != null) {
                successor.update(delta, location);
            }
        }

        private void apply() {
            float range = owner.getWeaponSentry().getRange(); 
            target = owner.getTargeting(position, direction.cpy().scl(range).add(position));
            width = range * target.getFraction();

            if (target.getTarget() != null) {
                // hit something
                target.getTarget().handleProjectile(this);
            }
        }
        
        @Override
        public void apply(Agent target) {
            float magnitude = getDamage(target)
                    * owner.getInventory().getRangedWeapon().getDamage();
            target.addEffect(new Stunned(owner, target, 0.2f));
            target.addEffect(new Bleed(owner, target, magnitude));
        }

        @Override
        public void render(float delta, OrthogonalTiledMapRenderer renderer) {
            if (target != null) {
                Batch batch = renderer.getBatch();
                batch.begin();
                batch.draw(texture, position.x, position.y, // position
                        0, 0, // origin
                        width, height, // size
                        1f, 1f, // scale
                        direction.angle()); // rotation
                batch.end();
            }
            
            if (successor != null) {
                successor.render(delta, renderer);
            }
        }

        @Override
        public float getZ() {
            return owner.getWeaponSentry().getZ() + Settings.EPSILON;
        }

        @Override
        public Vector2 getPosition() {
            return position;
        }

        @Override
        public boolean isFinished() {
            return elapsed >= FLASH_SECS;
        }

        @Override
        public void dispose() {
            // does nothing
        }

        @Override
        public Agent getOwner() {
            return owner;
        }

        @Override
        public void reset(Agent owner, Vector2 target) {
            if (reflected) {
                // cannot reflect again, to avoid an infinite loop
                return;
            }
            
            // we need to reflect the ray from the point of contact
            Vector2 contact = direction.cpy().scl(width).add(position);
            
            // perturb the bullet by a random amount
            float range = 30f;
            float rotation = (float) (Math.random() * range) - range / 2;
            Vector2 reflection = new Vector2(-direction.x, -direction.y).rotate(rotation);
            
            RangedWeaponRay ray = new RangedWeaponRay(owner, contact, reflection, true);
            successor = ray;
        }

        @Override
        public float getDamage(Agent target) {
            return DAMAGE_SCALE * owner.getAttackScale(target);
        }

        @Override
        public void cancel() {
            // does nothing
        }
    }
}
