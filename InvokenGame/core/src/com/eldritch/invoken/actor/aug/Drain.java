package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.HandledProjectile;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.effects.Draining;
import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.screens.GameScreen;

public class Drain extends ProjectileAugmentation {
    private static final float DAMAGE_SCALE = 25;
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

    public class DrainAction extends AnimatedAction {
        private final Vector2 target;

        public DrainAction(Agent actor, Vector2 target) {
            super(actor, Activity.Swipe, Drain.this);
            this.target = target;
        }

        @Override
        public void apply(Location location) {
            DrainBullet bullet = bulletPool.obtain();
            bullet.setup(owner, target);
            location.addEntity(bullet);
        }

        @Override
        public Vector2 getPosition() {
            return target;
        }
    }

    public static class DrainBullet extends HandledProjectile {
        private static final TextureRegion[] regions = GameScreen.getRegions(
                "sprite/effects/drain-attack.png", 32, 32)[0];
        private final Animation animation;

        public DrainBullet() {
            super(regions[0], 10, 0);
            animation = new Animation(0.1f, regions);
            animation.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
        }

        @Override
        protected void apply(Agent owner, Agent target) {
            target.addEffect(new Draining(owner, target, DAMAGE_SCALE, 2));
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
