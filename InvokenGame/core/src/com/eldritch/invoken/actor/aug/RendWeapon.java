package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.aug.Augmentation.ActiveAugmentation;
import com.eldritch.invoken.actor.items.MeleeWeapon;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.effects.Bleed;
import com.eldritch.invoken.effects.Slash;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.proto.Effects.DamageType;
import com.eldritch.invoken.ui.DebugEntityRenderer;
import com.eldritch.invoken.util.Damage;
import com.eldritch.invoken.util.Heuristics;
import com.eldritch.invoken.util.Settings;
import com.eldritch.invoken.util.SoundManager.SoundEffect;

public class RendWeapon extends ActiveAugmentation {
    private static final float DEFAULT_RANGE = 1.5f;
    private static final int DEFAULT_DAMAGE = 5;

    private static class Holder {
        private static final RendWeapon INSTANCE = new RendWeapon();
    }

    public static RendWeapon getInstance() {
        return Holder.INSTANCE;
    }

    private RendWeapon() {
        super("rend");
    }

    @Override
    public void prepare(Agent owner) {
        owner.toggleOn(RendWeapon.class);
    }

    @Override
    public void unprepare(Agent owner) {
        owner.toggleOff(RendWeapon.class);
    }

    public boolean isPrepared(Agent owner) {
        return owner.isToggled(RendWeapon.class);
    }

    @Override
    public Action getAction(Agent owner, Agent target) {
        return getAction(owner, target.getPosition());
    }

    @Override
    public Action getAction(Agent owner, Vector2 target) {
        return new RendAction(owner, target);
    }

    @Override
    public boolean isValid(Agent owner, Agent target) {
        return target != null && target != owner && owner.getInventory().hasMeleeWeapon();
    }

    @Override
    public boolean isValid(Agent owner, Vector2 target) {
        return owner.getInventory().hasMeleeWeapon();
    }

    @Override
    public int getCost(Agent owner) {
        return 1;
    }

    @Override
    public float quality(Agent owner, Agent target, Level level) {
        if (!target.isAlive() || owner == target) {
            return 0;
        }

        float range = getRange(owner);
        return owner.dst2(target) <= range ? Heuristics.randomizedDistanceScore(owner.dst2(target),
                range) : 0;
    }

    public class RendAction extends AnimatedAction {
        private final Vector2 strike = new Vector2();
        private final Vector2 target;
        private final Damage damage;

        public RendAction(Agent actor, Vector2 target) {
            super(actor, Activity.Swipe, RendWeapon.this);
            this.target = target;

            // update agent to fact the direction of their strike
            owner.setDirection(owner.getRelativeDirection(target));

            int damage = DEFAULT_DAMAGE;
            if (actor.getInventory().hasMeleeWeapon() && isPrepared(actor)) {
                MeleeWeapon weapon = actor.getInventory().getMeleeWeapon();
                damage = (int) weapon.getDamage();
            }
            this.damage = Damage.from(actor, DamageType.PHYSICAL, damage);
        }

        @Override
        public void apply(Level level) {
            float range = getRange(owner);
            float radius = range / 2;
            strike.set(owner.getPosition());
            strike.add(owner.getForwardVector().scl(radius));

            Vector2 center = getCenter(owner.getPosition(), target, range);
            owner.addEffect(new Slash(owner, center, range * 0.75f));
            InvokenGame.SOUND_MANAGER.playAtPoint(SoundEffect.MELEE_SWING, owner.getPosition());

            for (Agent neighbor : owner.getNeighbors()) {
                if (neighbor.inRange(center, radius)) {
                    neighbor.addEffect(new Bleed(neighbor, damage));
                    InvokenGame.SOUND_MANAGER.playAtPoint(SoundEffect.MELEE_HIT,
                            neighbor.getPosition());
                }
            }
        }

        @Override
        public void render(OrthogonalTiledMapRenderer renderer) {
            super.render(renderer);

            if (owner.getInventory().hasMeleeWeapon() && isPrepared(owner)) {
                // render weapon
                MeleeWeapon weapon = owner.getInventory().getMeleeWeapon();
                if (weapon.isVisible()) {
                    weapon.render(owner, Activity.Combat, getStateTime(), renderer);
                }
            }

            if (Settings.DEBUG_DRAW) {
                // debug: draw attack radius
                float range = getRange(owner);
                Vector2 center = getCenter(owner.getPosition(), target, range);
                DebugEntityRenderer.getInstance().renderCircle(center, range / 2,
                        renderer.getBatch().getProjectionMatrix());
            }
        }

        @Override
        protected float getHoldSeconds() {
            // this can be used as a tell for some enemy types
            return owner.getHoldSeconds();
        }

        @Override
        public float getPostHoldSeconds() {
            return 0.2f;
        }

        @Override
        public Vector2 getPosition() {
            return target;
        }
    }

    // Add half the range along the direction of the strike. The second half of
    // the range is the
    // radius.
    private static Vector2 getCenter(Vector2 origin, Vector2 target, float range) {
        Vector2 delta = target.cpy().sub(origin).nor().scl(range / 2);
        return origin.cpy().add(delta);
    }

    private float getRange(Agent owner) {
        if (!owner.getInventory().hasMeleeWeapon() || !isPrepared(owner)) {
            return DEFAULT_RANGE;
        }

        MeleeWeapon weapon = owner.getInventory().getMeleeWeapon();
        return weapon.getRange();
    }
}
