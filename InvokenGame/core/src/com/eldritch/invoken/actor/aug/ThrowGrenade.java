package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.aug.Augmentation.InstantAugmentation;
import com.eldritch.invoken.actor.items.Ammunition;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.actor.type.AoeProjectile;
import com.eldritch.invoken.effects.Detonation;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.proto.Effects.DamageType;
import com.eldritch.invoken.proto.Items.Item.RangedWeaponType;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.util.Damage;

public class ThrowGrenade extends InstantAugmentation {
    private static final int BASE_COST = 20;

    private static final String TOOLTIP = String.format("Grenade\n\n"
            + "Throws a grenade in front of the user or in the aimed direction that detonates "
            + "in a fiery explosion on impact or after a brief period of time has elapsed.\n\n"
            + "Mode: Instant\n" + "Cost: %d energy, 1 grenade", BASE_COST);

    private static class Holder {
        private static final ThrowGrenade INSTANCE = new ThrowGrenade();
    }

    public static ThrowGrenade getInstance() {
        return Holder.INSTANCE;
    }

    private ThrowGrenade() {
        super("throw");
    }

    @Override
    public Action getAction(Agent owner, Agent target) {
        Vector2 position;
        if (target == null || owner == target) {
            if (owner.isAiming()) {
                position = owner.getFocusPoint();
            } else {
                position = owner.getForwardVector().scl(5f).add(owner.getPosition());
            }
        } else {
            position = target.getPosition();
        }
        return getAction(owner, position);
    }

    @Override
    public Action getAction(Agent owner, Vector2 target) {
        return new ThrowAction(owner, target);
    }

    public boolean isValid(Agent owner) {
        return owner.getInventory().hasAmmunition(RangedWeaponType.GRENADE);
    }

    @Override
    public boolean isValid(Agent owner, Agent target) {
        return isValid(owner);
    }

    @Override
    public boolean isValid(Agent owner, Vector2 target) {
        return isValid(owner);
    }

    @Override
    public int getCost(Agent owner) {
        return BASE_COST;
    }

    @Override
    public float quality(Agent owner, Agent target, Level level) {
        return owner.getWeaponSentry().hasLineOfSight(target) ? 1 : 0;
    }

    @Override
    public String getTooltip() {
        return TOOLTIP;
    }

    @Override
    public String getLabel(Agent owner) {
        if (!owner.getInventory().hasAmmunition(RangedWeaponType.GRENADE)) {
            return "";
        }

        Ammunition ammo = owner.getInventory().getAmmunition(RangedWeaponType.GRENADE);
        int count = owner.getInventory().getItemCount(ammo);
        return count > 0 ? String.valueOf(count) : "";
    }

    public class ThrowAction extends AnimatedAction {
        private final Vector2 target;

        public ThrowAction(Agent actor, Vector2 target) {
            super(actor, Activity.Swipe, ThrowGrenade.this);
            this.target = target;
        }

        @Override
        public void apply(Level level) {
            // update agent to fact the direction of their strike
            owner.setDirection(owner.getRelativeDirection(target));

            Grenade bullet = new Grenade(owner, target);
            level.addEntity(bullet);

            // remove one grenade
            Ammunition grenade = owner.getInventory().getAmmunition(RangedWeaponType.GRENADE);
            owner.getInventory().removeItem(grenade, 1);
        }

        @Override
        public Vector2 getPosition() {
            return target;
        }
    }

    public static class Grenade extends AoeProjectile {
        private static final TextureRegion texture = new TextureRegion(
                GameScreen.getTexture("sprite/effects/grenade.png"));
        private static final TextureRegion[] explosionRegions = GameScreen.getMergedRegion(
                "sprite/effects/explosion.png", 256, 256);

        private static final int SPEED = 10;
        private static final int DAMAGE = 25;
        private static final int RADIUS = 3;

        public Grenade(Agent owner, Vector2 target) {
            super(owner, target, texture, explosionRegions, fixedSentryDirection(owner), SPEED,
                    Damage.from(owner, DamageType.THERMAL, DAMAGE), RADIUS);
        }

        @Override
        protected void onDetonate() {
            getOwner().getLocation().addEntity(new Detonation(getDamage(), getPosition(), RADIUS));
        }

        @Override
        protected void doDuringExplosion(float delta, Level level) {
        }
    }
}
