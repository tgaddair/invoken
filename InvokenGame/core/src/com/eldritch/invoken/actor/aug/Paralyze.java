package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.eldritch.invoken.actor.Projectile;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.effects.Paralyzed;
import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.screens.GameScreen;

public class Paralyze extends Augmentation {
    public Paralyze() {
        super("paralyze");
    }
    
	@Override
	public Action getAction(Agent owner, Agent target) {
		return new ParalyzeAction(owner, target);
	}
	
	@Override
	public boolean isValid(Agent owner, Agent target) {
		return target != null && target != owner && target.isAlive();
	}
	
	@Override
    public int getCost(Agent owner) {
        return 3;
    }
	
	@Override
    public float quality(Agent owner, Agent target, Location location) {
        return 1;
    }
	
	public class ParalyzeAction extends AnimatedAction {
		private final Agent target;
		
		public ParalyzeAction(Agent actor, Agent target) {
			super(actor, Activity.Swipe, Paralyze.this);
			this.target = target;
		}

		@Override
		public void apply(Location location) {
		    ParalyzeBullet bullet = bulletPool.obtain();
            bullet.setup(owner, target);
            location.addEntity(bullet);
		}
		
		@Override
        public Vector2 getPosition() {
            return target.getPosition();
        }
	}
	
	public static class ParalyzeBullet extends Projectile {
        private static final TextureRegion[] regions = GameScreen.getRegions(
                "sprite/effects/drain-attack.png", 32, 32)[0];
        private final Animation animation;

        public ParalyzeBullet() {
            super(1 / 32f * regions[0].getRegionWidth(), 1 / 32f * regions[0].getRegionWidth(), 10);

            animation = new Animation(0.1f, regions);
            animation.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
        }
        
        @Override
        protected void preRender(Batch batch) {
            batch.setColor(Color.GREEN);
        }
        
        @Override
        protected void postRender(Batch batch) {
            batch.setColor(Color.WHITE);
        }

        @Override
        protected void apply(Agent owner, Agent target) {
            target.addEffect(new Paralyzed(owner, target, 3));
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

    private static Pool<ParalyzeBullet> bulletPool = new Pool<ParalyzeBullet>() {
        @Override
        protected ParalyzeBullet newObject() {
            return new ParalyzeBullet();
        }
    };
}
