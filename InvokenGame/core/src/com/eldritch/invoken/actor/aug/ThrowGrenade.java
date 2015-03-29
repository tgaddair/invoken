package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.actor.type.AoeProjectile;
import com.eldritch.invoken.effects.Bleed;
import com.eldritch.invoken.effects.Stunned;
import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.proto.Effects.DamageType;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.util.Damage;

public class ThrowGrenade extends Augmentation {
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
        return getAction(owner, target.getPosition());
    }

    @Override
    public Action getAction(Agent owner, Vector2 target) {
        return new ThrowAction(owner, target);
    }

    @Override
    public boolean isValid(Agent owner, Agent target) {
        return target != null && target != owner;
    }

    @Override
    public boolean isValid(Agent owner, Vector2 target) {
        return true;
    }

    @Override
    public int getCost(Agent owner) {
        return 1;
    }

    @Override
    public float quality(Agent owner, Agent target, Location location) {
        return owner.getWeaponSentry().hasLineOfSight(target) ? 1 : 0;
    }

    public class ThrowAction extends AnimatedAction {
        private final Vector2 target;

        public ThrowAction(Agent actor, Vector2 target) {
            super(actor, Activity.Swipe, ThrowGrenade.this);
            this.target = target;
        }

        @Override
        public void apply(Location location) {
            // update agent to fact the direction of their strike
            owner.setDirection(owner.getRelativeDirection(target));

            Grenade bullet = new Grenade(owner, target);
            location.addEntity(bullet);
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

        public Grenade(Agent owner, Vector2 target) {
            super(owner, target, texture, explosionRegions, 5, Damage.from(owner,
                    DamageType.THERMAL, 50), 3);
        }

        @Override
        protected void onDetonate() {
            // friendly fire
            detonateOn(getOwner());
            for (Agent neighbor : getOwner().getNeighbors()) {
                detonateOn(neighbor);
            }
        }

        private void detonateOn(Agent agent) {
            Agent owner = getOwner();
            if (agent.inRange(getPosition(), getRadius())) {
                Vector2 direction = agent.getPosition().cpy().sub(getPosition()).nor();
                agent.applyForce(direction.scl(500));
                agent.addEffect(new Stunned(owner, agent, 0.2f));
                agent.addEffect(new Bleed(agent, getDamage()));
            }
        }

        @Override
        protected void doDuringExplosion(float delta, Location location) {
        }
    }
}
