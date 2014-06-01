package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Pool;
import com.eldritch.invoken.actor.Agent;
import com.eldritch.invoken.actor.Projectile;
import com.eldritch.invoken.actor.Agent.Activity;
import com.eldritch.invoken.effects.Draining;
import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.screens.GameScreen;

public class Drain extends Augmentation {
    public Drain() {
        super("drain");
    }

    @Override
    public Action getAction(Agent owner, Agent target) {
        return new DrainAction(owner, target);
    }

    @Override
    public boolean isValid(Agent owner, Agent target) {
        return target != null && target != owner && target.isAlive();
    }

    public class DrainAction extends AnimatedAction {
        private final Agent target;

        public DrainAction(Agent actor, Agent target) {
            super(actor, Activity.Swipe);
            this.target = target;
        }

        @Override
        public void apply(Location location) {
            DrainBullet bullet = bulletPool.obtain();
            bullet.setup(owner, target);
            location.addEntity(bullet);
        }
    }

    public static class DrainBullet extends Projectile {
        private static final TextureRegion[] regions = GameScreen.getRegions(
                "sprite/effects/drain-attack.png", 64, 64)[0];
        private final Animation animation;

        public DrainBullet() {
            super(1 / 32f * regions[0].getRegionWidth(), 1 / 32f * regions[0].getRegionWidth(), 10);

            animation = new Animation(0.1f, regions);
            animation.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
        }

        @Override
        protected void apply(Agent owner, Agent target) {
            target.addEffect(new Draining(owner, target, 5, 2));
        }

        @Override
        protected TextureRegion getTexture(float stateTime) {
            return animation.getKeyFrame(stateTime);
        }

        @Override
        protected void free() {
            bulletPool.free(this);
        }
    }

    private static Pool<DrainBullet> bulletPool = new Pool<DrainBullet>() {
        @Override
        protected DrainBullet newObject() {
            return new DrainBullet();
        }
    };
}
