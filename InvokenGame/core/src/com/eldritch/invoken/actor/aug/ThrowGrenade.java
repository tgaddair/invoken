package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.actor.type.Projectile;
import com.eldritch.invoken.effects.Bleed;
import com.eldritch.invoken.effects.Stunned;
import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.screens.GameScreen;

public class ThrowGrenade extends Augmentation {
    public ThrowGrenade() {
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
        return 1;
    }

    public class ThrowAction extends AnimatedAction {
        private final Vector2 target;

        public ThrowAction(Agent actor, Vector2 target) {
            super(actor, Activity.Swipe, ThrowGrenade.this);
            this.target= target;
        }

        @Override
        public void apply(Location location) {
            // update agent to fact the direction of their strike
            owner.setDirection(owner.getRelativeDirection(target));
            
            Grenade bullet = bulletPool.obtain();
            bullet.setup(owner, target);
            location.addEntity(bullet);
        }
        
        @Override
        public Vector2 getPosition() {
            return target;
        }
    }
    
    public static class Grenade extends Projectile {
        private static final TextureRegion[] regions = GameScreen.getRegions(
                "sprite/effects/drain-attack.png", 32, 32)[0];
        private static final TextureRegion[] explosionRegions = GameScreen.getMergedRegion(
        		"sprite/effects/explosion.png", 256, 256);
        
        private final Animation animation;
        private final Animation explosion;
        
        private Vector2 target = null;
        private float radius = 3;
        private float explosionTime = 0;
        private boolean detonated = false;

        public Grenade() {
            super(1 / 32f * regions[0].getRegionWidth(), 1 / 32f * regions[0].getRegionWidth(),
            		5, 50);

            animation = new Animation(0.1f, regions);
            animation.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
            
            explosion = new Animation(0.1f, explosionRegions);
        }
        
        @Override
        public void setup(Agent source, Vector2 target) {
        	super.setup(source, target);
        	this.target = target;
        	explosionTime = 0;
        	detonated = false;
        }
        
        @Override
        public boolean handleBeforeUpdate(float delta, Location location) {
        	if (detonated) {
        		// update the explosion
        		explosionTime += delta;
        		
        		// cancel the projectile if we're done with the explosion
        		if (explosion.isAnimationFinished(explosionTime)) {
        			cancel();
        		}
        	} else if (position.dst2(target) < 1) {
        		// special case where the position is very close to the target
        		detonate();
        	}
        	return detonated;
        }
        
        @Override
        public void render(float delta, OrthogonalTiledMapRenderer renderer) {
        	if (detonated) {
        		// render the explosion
        		float width = radius * 2;
        		float height = radius * 2;
        		Batch batch = renderer.getSpriteBatch();
                batch.begin();
                batch.draw(explosion.getKeyFrame(explosionTime),
                		position.x - width / 2, position.y - height / 2, width, height);
                batch.end();
        	} else {
        		// render the projectile itself
        		super.render(delta, renderer);
        	}
        }

        @Override
        protected TextureRegion getTexture(float stateTime) {
            return animation.getKeyFrame(stateTime);
        }

        @Override
        protected void free() {
            bulletPool.free(this);
        }

		@Override
		protected void handleAgentContact(Agent agent) {
			detonate();
		}

		@Override
		protected void handleObstacleContact() {
			detonate();
		}
		
		private void detonate() {
			// friendly fire
			detonateOn(getOwner());
			for (Agent neighbor : getOwner().getNeighbors()) {
				detonateOn(neighbor);
        	}
			detonated = true;
		}
		
		private void detonateOn(Agent agent) {
			Agent owner = getOwner();
			if (agent.inRange(getPosition(), radius)) {
				Vector2 direction = agent.getPosition().cpy().sub(getPosition()).nor();
				agent.applyForce(direction.scl(500));
				agent.addEffect(new Stunned(owner, agent, 0.2f));
				agent.addEffect(new Bleed(owner, agent, getDamage(agent)));
    		}
		}
    }

    private static Pool<Grenade> bulletPool = new Pool<Grenade>() {
        @Override
        protected Grenade newObject() {
            return new Grenade();
        }
    };
}
