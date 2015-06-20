package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.items.MeleeWeapon;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.effects.Bleed;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.proto.Effects.DamageType;
import com.eldritch.invoken.util.Damage;
import com.eldritch.invoken.util.Heuristics;
import com.eldritch.invoken.util.SoundManager.SoundEffect;

public class RendWeapon extends Augmentation {
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
        if (!target.isAlive()) {
            return 0;
        }

        MeleeWeapon weapon = owner.getInventory().getMeleeWeapon();
        return owner.dst2(target) <= weapon.getRange() ? Heuristics.randomizedDistanceScore(
                owner.dst2(target), weapon.getRange()) : 0;
    }

    public class RendAction extends AnimatedAction {
        private final Vector2 strike = new Vector2();
        private final Vector2 target;
        private final Damage damage;

        public RendAction(Agent actor, Vector2 target) {
            super(actor, Activity.Swipe, RendWeapon.this);
            this.target = target;

            MeleeWeapon weapon = actor.getInventory().getMeleeWeapon();
            this.damage = Damage.from(actor, DamageType.PHYSICAL, (int) weapon.getDamage());
        }

        @Override
        public void apply(Level level) {
            MeleeWeapon weapon = owner.getInventory().getMeleeWeapon();
            strike.set(owner.getPosition());
            strike.add(owner.getForwardVector().scl(weapon.getRange() / 2));

            Vector2 center = getCenter(owner.getPosition(), target, weapon.getRange());

            // update agent to fact the direction of their strike
            owner.setDirection(owner.getRelativeDirection(center));
            InvokenGame.SOUND_MANAGER.playAtPoint(SoundEffect.MELEE_SWING, owner.getPosition());

            for (Agent neighbor : owner.getNeighbors()) {
                if (neighbor.inRange(center, weapon.getRange() / 5)) {
                    neighbor.addEffect(new Bleed(neighbor, damage));
                    InvokenGame.SOUND_MANAGER.playAtPoint(SoundEffect.MELEE_HIT,
                            neighbor.getPosition());
                }
            }
        }

        @Override
        public void render(OrthogonalTiledMapRenderer renderer) {
            super.render(renderer);

            // render weapon
            MeleeWeapon weapon = owner.getInventory().getMeleeWeapon();
            if (weapon.isVisible()) {
                weapon.render(owner, Activity.Combat, getStateTime(), renderer);
            }

            // debug: draw attack radius
            // Vector2 center = getCenter(owner.getPosition(), target, weapon.getRange());
            // DebugEntityRenderer.getInstance().renderCircle(center, weapon.getRange() / 5,
            // renderer.getSpriteBatch().getProjectionMatrix());
        }

        @Override
        public Vector2 getPosition() {
            return target;
        }
    }

    // Add half the range along the direction of the strike. The second half of the range is the
    // radius.
    private static Vector2 getCenter(Vector2 origin, Vector2 target, float range) {
        Vector2 delta = target.cpy().sub(origin).nor().scl(range / 2);
        return origin.cpy().add(delta);
    }
}
