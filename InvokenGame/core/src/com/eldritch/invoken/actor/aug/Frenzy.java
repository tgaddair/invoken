package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Projectile;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.effects.Frenzied;
import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.screens.GameScreen;

public class Frenzy extends Augmentation {
    public Frenzy() {
        super("frenzy");
    }
    
	@Override
	public Action getAction(Agent owner, Agent target, Vector2 position) {
		return new FrenzyAction(owner, target);
	}
	
	@Override
	public boolean isValid(Agent owner, Agent target) {
		return target != null && target != owner && target.isAlive();
	}
	
	@Override
    public int getCost(Agent owner) {
        return 2;
    }
	
    @Override
    public float quality(Agent owner, Agent target, Location location) {
        return 1;
    }
	
	public class FrenzyAction extends AnimatedAction {
		private final Agent target;
		
		public FrenzyAction(Agent actor, Agent target) {
			super(actor, Activity.Swipe, Frenzy.this);
			this.target = target;
		}

		@Override
		public void apply(Location location) {
		    FrenzyBullet bullet = bulletPool.obtain();
            bullet.setup(owner, target);
            location.addEntity(bullet);
		}
		
		@Override
        public Vector2 getPosition() {
            return target.getPosition();
        }
	}
	
	public static class FrenzyBullet extends Projectile {
        private static final TextureRegion[] regions = GameScreen.getRegions(
                "sprite/effects/drain-attack.png", 32, 32)[0];
        private final Animation animation;

        public FrenzyBullet() {
            super(1 / 32f * regions[0].getRegionWidth(), 1 / 32f * regions[0].getRegionWidth(),
                    10, 0);

            animation = new Animation(0.1f, regions);
            animation.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
        }
        
        @Override
        protected void preRender(Batch batch) {
            batch.setColor(Color.RED);
        }
        
        @Override
        protected void postRender(Batch batch) {
            batch.setColor(Color.WHITE);
        }

        @Override
        protected void apply(Agent owner, Agent target) {
            target.addEffect(new Frenzied(owner, target, 3));
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

    private static Pool<FrenzyBullet> bulletPool = new Pool<FrenzyBullet>() {
        @Override
        protected FrenzyBullet newObject() {
            return new FrenzyBullet();
        }
    };
}
