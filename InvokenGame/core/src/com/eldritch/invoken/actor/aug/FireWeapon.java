package com.eldritch.invoken.actor.aug;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.AgentHandler;
import com.eldritch.invoken.actor.items.RangedWeapon;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.RayTarget;
import com.eldritch.invoken.actor.type.HandledProjectile;
import com.eldritch.invoken.actor.type.TemporaryEntity;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.actor.type.Agent.Direction;
import com.eldritch.invoken.effects.Bleed;
import com.eldritch.invoken.effects.HoldingWeapon;
import com.eldritch.invoken.effects.Stunned;
import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.util.Settings;

public class FireWeapon extends ProjectileAugmentation {
    private static final float DAMAGE_SCALE = 1;
    private static final float BULLET_VELOCITY = 25;
    private static final int BASE_COST = 10;
    private static final float ALERT_RADIUS = 10;
    
    private static class Holder {
        private static final FireWeapon INSTANCE = new FireWeapon();
	}
	
	public static FireWeapon getInstance() {
		return Holder.INSTANCE;
	}

    private FireWeapon() {
        super("fire");
    }
    
    @Override
    public void prepare(Agent owner) {
        // add an effect that shows a rotating weapon
        owner.toggleOn(HoldingWeapon.class);
        owner.addEffect(new HoldingWeapon(owner));
    }
    
    @Override
    public void unprepare(Agent owner) {
        owner.toggleOff(HoldingWeapon.class);
    }

    @Override
    public Action getAction(Agent owner, Vector2 target) {
        return new FireAction(owner, target);
    }
    
    @Override
    public boolean isValid(Agent owner) {
		return super.isValid(owner) && owner.getInventory().canUseRangedWeapon();
	}
    
    @Override
    public boolean isValid(Agent owner, Vector2 target) {
        return super.isValid(owner, target) && isValid(owner);
    }

    @Override
    public boolean isValid(Agent owner, Agent target) {
        return super.isValid(owner, target) && isValid(owner);
    }
    
    @Override
    public int getCost(Agent owner) {
        return BASE_COST;
    }

    public class FireAction extends AnimatedAction {
        private final Vector2 target;
        private final float width;
        private final float height;
        private final Map<Direction, Animation> animations = new HashMap<Direction, Animation>();

        public FireAction(Agent actor, Vector2 target) {
            super(actor, Activity.Combat, FireWeapon.this);
            this.target = target;

            TextureRegion[][] regions = GameScreen.getRegions("sprite/effects/muzzle-flash.png",
                    48, 48);
            for (Direction d : Direction.values()) {
                Animation anim = new Animation(0.05f, regions[d.ordinal()]);
                anim.setPlayMode(Animation.PlayMode.NORMAL);
                animations.put(d, anim);
            }

            width = 1 / 32f * regions[0][0].getRegionWidth();
            height = 1 / 32f * regions[0][0].getRegionHeight();
        }

        @Override
        public void render(OrthogonalTiledMapRenderer renderer) {
            super.render(renderer);

//            // draw the muzzle flash
//            TextureRegion frame = getAnimation().getKeyFrame(stateTime);
//            Vector2 position = owner.getRenderPosition();
//
//            Batch batch = renderer.getSpriteBatch();
//            batch.begin();
//            batch.draw(frame, position.x - width / 2, position.y - height / 2, width, height);
//            batch.end();
//
//            // render weapon
//            owner.getInventory().getRangedWeapon()
//                    .render(owner, Activity.Combat, getStateTime(), renderer);
        }

        @Override
        public boolean isFinished() {
            return getAnimation().isAnimationFinished(stateTime);
        }

        @Override
        protected boolean canApply() {
            Animation anim = getAnimation();
            return anim.getKeyFrameIndex(stateTime) == 2;
        }

        @Override
        public void apply(Location location) {
            // add bullet to scene
//            RangedWeaponBullet bullet = new RangedWeaponBullet(owner);
            RangedWeaponRay bullet = new RangedWeaponRay(owner);
            location.addEntity(bullet);
            
            // update agent to fact the direction of their shots
            owner.setDirection(owner.getRelativeDirection(target));
            
            // add camera shake
            owner.recoil();
            
            // add cooldown to weapon
            RangedWeapon weapon = owner.getInventory().getRangedWeapon();
            owner.getInventory().setCooldown(weapon, weapon.getCooldown());
            
            // alert all enemies in range if the weapon is not silenced
            for (Agent neighbor : owner.getNeighbors()) {
                if (owner.dst2(neighbor) < ALERT_RADIUS * ALERT_RADIUS) {
                    neighbor.alertTo(owner);
                }
            }
        }
        
        @Override
        public Vector2 getPosition() {
            return target;
        }
        
        private Animation getAnimation() {
            return animations.get(owner.getDirection());
        }
    }

    public static class RangedWeaponBullet extends HandledProjectile {
        private static final TextureRegion texture = new TextureRegion(
                GameScreen.getTexture("sprite/effects/bullet1.png"));

        public RangedWeaponBullet(Agent owner) {
            super(owner, texture, BULLET_VELOCITY, DAMAGE_SCALE);
        }

        @Override
        protected void apply(Agent owner, Agent target) {
        	float magnitude = getDamage(target) * owner.getInventory().getRangedWeapon().getDamage();
            target.applyForce(velocity.cpy().nor().scl(100));
            target.addEffect(new Stunned(owner, target, 0.2f));
            target.addEffect(new Bleed(owner, target, magnitude));
        }

        @Override
        protected TextureRegion getTexture(float stateTime) {
            return texture;
        }
    }
    
    public static class RangedWeaponRay implements TemporaryEntity {
        private static final float FLASH_SECS = 1f;
        private static final TextureRegion texture = new TextureRegion(
                GameScreen.getTexture("sprite/effects/bullet2.png"));
        
        private final Vector2 position = new Vector2();
        private final Vector2 direction = new Vector2();
        private final Agent owner;
        
        private RayTarget target = null;
        private float elapsed = 0;
        
        private final float height = 0.1f;
        private float width = 0;
        
        public RangedWeaponRay(Agent owner) {
            this.owner = owner;
            
            // the owner may move after firing, but this vapor trail should not
            this.position.set(owner.getWeaponSentry().getPosition());
            this.direction.set(owner.getWeaponSentry().getDirection());
        }

        @Override
        public void update(float delta, Location location) {
            if (target == null) {
                apply();
            } else {
                elapsed += delta;
            }
        }
        
        private void apply() {
            target = owner.getTargeting();
            
            float fraction = target.getFraction();
            width = owner.getWeaponSentry().getTargetingVector().sub(position).scl(fraction).len();
        }

        @Override
        public void render(float delta, OrthogonalTiledMapRenderer renderer) {
            if (target != null) {
                Batch batch = renderer.getBatch();
                batch.begin();
                batch.draw(texture,
                        position.x, position.y,  // position
                        0, 0,  // origin
                        width, height,  // size
                        1f, 1f,  // scale
                        direction.angle());  // rotation
                batch.end(); 
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
        }
    }
}
