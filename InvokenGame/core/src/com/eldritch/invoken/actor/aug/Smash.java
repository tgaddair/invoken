package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.items.MeleeWeapon;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.effects.Bleed;
import com.eldritch.invoken.effects.Stunned;
import com.eldritch.invoken.gfx.AnimatedEntity;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.proto.Effects.DamageType;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.ui.DebugEntityRenderer;
import com.eldritch.invoken.util.Damage;
import com.eldritch.invoken.util.Heuristics;
import com.eldritch.invoken.util.Settings;
import com.eldritch.invoken.util.SoundManager.SoundEffect;

public class Smash extends Augmentation {
    private static final TextureRegion[] SMOKE_REGIONS = GameScreen.getMergedRegion(
            "sprite/effects/smoke-ring.png", 128, 128);

    private static class Holder {
        private static final Smash INSTANCE = new Smash();
    }

    public static Smash getInstance() {
        return Holder.INSTANCE;
    }

    private Smash() {
        super(false);
    }

    @Override
    public Action getAction(Agent owner, Agent target) {
        return getAction(owner, target.getPosition());
    }

    @Override
    public Action getAction(Agent owner, Vector2 target) {
        return new SmashAction(owner, target);
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

        MeleeWeapon weapon = owner.getInventory().getMeleeWeapon();
        return owner.dst2(target) <= weapon.getRange() ? Heuristics.randomizedDistanceScore(
                owner.dst2(target), weapon.getRange()) : 0;
    }

    public class SmashAction extends AnimatedAction {
        private final Vector2 strike = new Vector2();
        private final Vector2 target;

        public SmashAction(Agent actor, Vector2 target) {
            super(actor, Activity.Swipe, Smash.this);
            this.target = target;

            // update agent to fact the direction of their strike
            owner.setDirection(owner.getRelativeDirection(target));

            MeleeWeapon weapon = actor.getInventory().getMeleeWeapon();
        }

        @Override
        public void apply(Level level) {
            MeleeWeapon weapon = owner.getInventory().getMeleeWeapon();
            float range = getRange(weapon);
            float radius = range / 2;
            strike.set(owner.getPosition());
            strike.add(owner.getForwardVector().scl(radius));

            Vector2 center = getCenter(owner.getPosition(), target, range);
            owner.getLocation().addEntity(
                    new AnimatedEntity(SMOKE_REGIONS, center, new Vector2(range * 1.5f,
                            range * 1.5f), 0.05f));
            InvokenGame.SOUND_MANAGER.playAtPoint(SoundEffect.MELEE_SWING, owner.getPosition());

            Vector2 direction = new Vector2();
            for (Agent neighbor : owner.getNeighbors()) {
                if (neighbor.inRange(center, radius)) {
                    float scale = Heuristics.distanceScore(center.dst2(neighbor.getPosition()), 0);
                    direction.set(neighbor.getPosition()).sub(owner.getPosition()).nor().scl(scale);
                    neighbor.applyForce(direction.scl(500));
                    neighbor.addEffect(new Stunned(owner, neighbor, 0.2f));

                    Damage damage = Damage.from(owner, DamageType.PHYSICAL,
                            (int) (weapon.getDamage() * scale));
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

            if (Settings.DEBUG_DRAW) {
                // debug: draw attack radius
                float range = getRange(weapon);
                Vector2 center = getCenter(owner.getPosition(), target, range);
                DebugEntityRenderer.getInstance().renderCircle(center, range / 2,
                        renderer.getBatch().getProjectionMatrix());
            }
        }

        @Override
        protected float getHoldSeconds() {
            // this can be used as a tell for some enemy types
            return owner.getAttackSpeed() >= 2 ? 0 : 1f / owner.getAttackSpeed();
        }

        @Override
        public float getPostHoldSeconds() {
            return 0.5f;
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

    private static float getRange(MeleeWeapon weapon) {
        return weapon.getRange();
    }
}