package com.eldritch.invoken.actor.items;

import java.util.HashMap;
import java.util.List;
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
import com.eldritch.invoken.actor.AgentInventory;
import com.eldritch.invoken.effects.Bleed;
import com.eldritch.invoken.effects.Stunned;
import com.eldritch.invoken.location.Location;
import com.eldritch.invoken.proto.Effects.DamageType;
import com.eldritch.invoken.proto.Items;
import com.eldritch.invoken.proto.Items.Item.DamageMod;
import com.eldritch.invoken.proto.Items.Item.RangedWeaponType;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.util.Damage;
import com.eldritch.invoken.util.Settings;
import com.eldritch.invoken.util.SoundManager.SoundEffect;
import com.google.common.base.Strings;

public abstract class RangedWeapon extends Item {
    private static final float BASE_COST = 5f;
    private static final float BULLET_VELOCITY = 25;

    private static final TextureRegion BULLET_TEXTURE = new TextureRegion(
            GameScreen.getTexture("sprite/effects/bullet1.png"));
    private static final TextureRegion RAY_TEXTURE = new TextureRegion(
            GameScreen.getTexture("sprite/effects/bullet2.png"));

    private final Map<Direction, Animation> animations = new HashMap<Direction, Animation>();
    private final TextureRegion texture;
    private final DamageType primary;
    private final int baseCost;

    public RangedWeapon(Items.Item item) {
        this(item, getRegion(item.getAsset()));
    }

    public RangedWeapon(Items.Item item, TextureRegion texture) {
        super(item, texture);
        this.texture = texture;

        // get primary damage type
        float damageSum = 0;
        DamageMod greatest = null;
        for (DamageMod mod : getData().getDamageModifierList()) {
            damageSum += mod.getMagnitude();
            if (greatest == null || mod.getMagnitude() > greatest.getMagnitude()) {
                greatest = mod;
            }
        }
        primary = greatest != null ? greatest.getDamage() : DamageType.PHYSICAL;
        baseCost = (int) (BASE_COST + damageSum / 5);
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
        batch.draw(texture.getTexture(), position.x - width / 2 - dx, position.y - height / 2, // position
                width / 2 + dx, height / 2, // origin
                width, height, // size
                1f, 1f, // scale
                theta, // rotation
                0, 0, // srcX, srcY
                texture.getRegionWidth(), texture.getRegionHeight(), // srcWidth, srcHeight
                false, flipY); // flipX, flipY
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

    public List<DamageMod> getDamageList() {
        return getData().getDamageModifierList();
    }

    public RangedWeaponType getType() {
        return data.getRangedType();
    }

    public float getDamage() {
        float damage = 0;
        for (DamageMod mod : getData().getDamageModifierList()) {
            damage += mod.getMagnitude();
        }
        return damage;
    }

    public int getBaseCost() {
        return baseCost;
    }

    public float getCooldown() {
        return (float) getData().getCooldown();
    }

    @Override
    public boolean isEquipped(AgentInventory inventory) {
        return inventory.getRangedWeapon() == this;
    }

    @Override
    public void equipFrom(AgentInventory inventory) {
        inventory.setRangedWeapon(this);
    }

    @Override
    public void unequipFrom(AgentInventory inventory) {
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
        StringBuilder result = new StringBuilder(String.format("%s\n" + "Range: %.2f\n",
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

    public class RangedWeaponBullet extends HandledBullet {
        public RangedWeaponBullet(Agent owner) {
            super(owner, texture, BULLET_VELOCITY, Damage.from(owner, RangedWeapon.this));
        }

        @Override
        protected void apply(Agent owner, Agent target) {
            // TODO: rail gun should continue on through enemies
            target.addEffect(new Stunned(owner, target, 0.2f));
            target.addEffect(new Bleed(target, getDamage(), velocity.cpy().nor().scl(250)));
        }

        @Override
        protected TextureRegion getTexture(float stateTime) {
            return BULLET_TEXTURE;
        }
    }

    public class RangedWeaponRay implements HandledProjectile {
        private static final float FLASH_SECS = 0.075f;

        private final Vector2 position = new Vector2();
        private final Vector2 direction = new Vector2();

        private final Agent owner;
        private final boolean reflected;
        private final Damage damage;

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
            this.damage = Damage.from(owner, RangedWeapon.this);

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
            // TODO: strange bug that occurs under somewhat uncertain circumstances
            // sometimes a ray will pass right through the center of the target without being
            // recognized as a valid collision; it appears to be related to being targeted by the
            // weapon pointing to the left, and when there is an obstruction occluding the shot
            // that lacks the right category bits to register a collision itself
            // notice that the targeting reticle will not pass
            // through it, even though it is marked as a low obstacle
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
            target.addEffect(new Stunned(owner, target, 0.2f));
            target.addEffect(new Bleed(target, getDamage()));
        }

        @Override
        public void render(float delta, OrthogonalTiledMapRenderer renderer) {
            if (target != null) {
                Batch batch = renderer.getBatch();
                batch.begin();
                batch.draw(RAY_TEXTURE, position.x, position.y, // position
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
        public Damage getDamage() {
            return damage;
        }

        @Override
        public void cancel() {
            // does nothing
        }
    }
    
    public abstract SoundEffect getSoundEffect();
    
    public static class Pistol extends RangedWeapon {
        public Pistol(Items.Item item) {
            super(item);
        }
        
        @Override
        public SoundEffect getSoundEffect() {
            return SoundEffect.RANGED_WEAPON_SMALL;
        }
    }
    
    public static class Rifle extends RangedWeapon {
        public Rifle(Items.Item item) {
            super(item);
        }
        
        @Override
        public SoundEffect getSoundEffect() {
            return SoundEffect.RANGED_WEAPON_RIFLE;
        }
    }

    public static RangedWeapon from(Items.Item item) {
        switch (item.getRangedType()) {
            case PISTOL:
                return new Pistol(item);
            case RIFLE:
                return new Rifle(item);
            default:
                throw new IllegalArgumentException("Unrecognized ranged weapon type: "
                        + item.getRangedType());
        }
    }
}
