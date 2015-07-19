package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.actor.type.HandledBullet;
import com.eldritch.invoken.box2d.Bullet;
import com.eldritch.invoken.effects.Draining;
import com.eldritch.invoken.gfx.AnimatedEntity;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.proto.Effects.DamageType;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.util.Damage;
import com.eldritch.invoken.util.Heuristics;
import com.eldritch.invoken.util.Utils;

public class Drain extends ProjectileAugmentation {
    public static final TextureRegion[] SPLASH_REGIONS = GameScreen.getMergedRegion(
            "sprite/effects/drain1.png", 120, 120);
    
    private static final int DAMAGE_SCALE = 25;
    private static final int BASE_COST = 10;

    private static class Holder {
        private static final Drain INSTANCE = new Drain();
    }

    public static Drain getInstance() {
        return Holder.INSTANCE;
    }

    private Drain() {
        super("drain");
    }

    @Override
    public Action getAction(Agent owner, Vector2 target) {
        return new DrainAction(owner, target);
    }

    @Override
    public int getCost(Agent owner) {
        return BASE_COST;
    }

    @Override
    public float quality(Agent owner, Agent target, Level level) {
        if (!target.isAlive() || target == owner) {
            return 0;
        }

        float idealDst = 3f;
        return Heuristics.randomizedDistanceScore(owner.dst2(target), idealDst * idealDst);
    }

    public class DrainAction extends AnimatedAction {
        private final Vector2 target;

        public DrainAction(Agent actor, Vector2 target) {
            super(actor, Activity.Swipe, Drain.this);
            this.target = target;
        }

        @Override
        public void apply(Level level) {
            DrainBullet bullet = new DrainBullet(owner);
            level.addEntity(bullet);
        }

        @Override
        public Vector2 getPosition() {
            return target;
        }
    }

    public static class DrainBullet extends HandledBullet {
        private static final float BULLET_SIZE = 1.5f;
        private static final float SPLASH_SIZE = 4f;
        private static final float MAX_SEEK_DST2 = 25f;
        private static final float ADJUSTMENT_STEP = 0.05f;
        private static final float V_SCALE = 5f;
        private static final float V_MAX = 15f;

        private static final TextureRegion region = new TextureRegion(
                GameScreen.getTexture("sprite/effects/infect.png"));
        private final Vector2 adjustment = new Vector2();
        private float lastAdjustment = 0;

        public DrainBullet(Agent owner) {
            super(owner, region, BULLET_SIZE, V_MAX, Damage.from(owner, DamageType.VIRAL,
                    getBaseDamage(owner)));
        }

        @Override
        protected void apply(Agent owner, Agent target) {
            target.addEffect(new Draining(target, getDamage(), 2));
        }

        @Override
        public boolean handleBeforeUpdate(float delta, Level level) {
            lastAdjustment += delta;

            // adjust the trajectory of the bullet at certain intervals
            if (lastAdjustment > ADJUSTMENT_STEP) {
                Bullet bullet = getBullet();
                Agent nearest = getNearestNeighbor(bullet.getPosition());
                if (nearest != null) {
                    // adjust the velocity of the bullet to seek the nearest
                    // target
                    adjustment.set(nearest.getPosition()).sub(bullet.getPosition());
                    bullet.setVelocity(adjustment.add(bullet.getVelocity().scl(V_SCALE)
                            .clamp(0, V_MAX)));
                }
                lastAdjustment = 0;
            }

            return false;
        }

        private Agent getNearestNeighbor(Vector2 position) {
            float bestDistance = MAX_SEEK_DST2;
            Agent nearest = null;
            for (Agent neighbor : getOwner().getNeighbors()) {
                if (!neighbor.isAlive() || !getOwner().isEnemy(neighbor)) {
                    continue;
                }

                float distance = neighbor.getPosition().dst2(position);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    nearest = neighbor;
                }
            }
            return nearest;
        }

        @Override
        protected TextureRegion getTexture(float stateTime) {
            return region;
        }

        @Override
        protected void onFinish() {
            Vector2 size = Utils.getSize(SPLASH_REGIONS[0], SPLASH_SIZE);
            getOwner().getLocation().addEntity(
                    new AnimatedEntity(SPLASH_REGIONS, getPosition().cpy().add(0, size.y / 4),
                            size, 0.025f));
        }

        private static int getBaseDamage(Agent owner) {
            return (int) (DAMAGE_SCALE * owner.getInfo().getExecuteModifier());
        }
    }
}
