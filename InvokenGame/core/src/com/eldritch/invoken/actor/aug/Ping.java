package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.aug.Augmentation.SelfAugmentation;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.actor.type.TemporaryEntity;
import com.eldritch.invoken.effects.BasicEffect;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.util.SoundManager.SoundEffect;

public class Ping extends SelfAugmentation {
    private static final int COST = 10;
    
	private static class Holder {
        private static final Ping INSTANCE = new Ping();
	}
	
	public static Ping getInstance() {
		return Holder.INSTANCE;
	}
	
    private Ping() {
        super("ping");
    }
    
	@Override
	public boolean isValid(Agent owner, Agent target) {
	    return true;
	}

	@Override
	public boolean isValid(Agent owner, Vector2 target) {
	    return true;
	}

	@Override
	public Action getAction(Agent owner, Agent target) {
		return new PingAction(owner);
	}
	
	@Override
	public Action getAction(Agent owner, Vector2 target) {
		return new PingAction(owner);
	}
	
	@Override
    public int getCost(Agent owner) {
        return COST;
    }
	
    @Override
    public float quality(Agent owner, Agent target, Level level) {
        return owner.hasTarget() ? 0 : 1;
    }
	
	public class PingAction extends AnimatedAction {
		public PingAction(Agent actor) {
			super(actor, Activity.Cast, Ping.this);
		}

		@Override
		public void apply(Level level) {
		    // find nearest hostile
		    Agent nearest = null;
		    float bestDst2 = Float.POSITIVE_INFINITY;
		    for (Agent agent : level.getAllAgents()) {
		        if (owner != agent && agent.isAlive() && hasAnimosity(agent)) {
		            float dst2 = agent.dst2(owner);
		            if (dst2 < bestDst2) {
		                nearest = agent;
		                bestDst2 = dst2;
		            }
		        }
		    }
		    
		    // notify the owner of the hostile's current relative direction
		    if (nearest != null) {
		        owner.addEffect(new Pinging(owner, nearest));
		    }
		    
		    // remove the selected state of this aug, as it is not sustained
		    owner.getInfo().getAugmentations().removeSelfAugmentation(Ping.this);
		    InvokenGame.SOUND_MANAGER.playAtPoint(SoundEffect.INVALID, owner.getPosition(), 2.5f);
		}
		
		@Override
        public Vector2 getPosition() {
            return owner.getPosition();
        }
		
		private boolean hasAnimosity(Agent other) {
		    return owner.isEnemy(other) || other.isEnemy(owner);
		}
	}
	
	public static class Pinging extends BasicEffect {
	    private static final float DURATION = 3f;
	    
	    private final Agent nearest;
	    private final PingIndicator indicator;
	    private float elapsed = 0;
	    
	    public Pinging(Agent actor, Agent nearest) {
	        super(actor);
	        this.nearest = nearest;
	        this.indicator = new PingIndicator(target, nearest);
	    }
	    
	    @Override
	    public void doApply() {
	        target.getLocation().addEntity(indicator);
	        target.locate(nearest);
	    }
	    
	    @Override
	    public void dispel() {
	        indicator.cancel();
	    }
	    
	    @Override
	    public boolean isFinished() {
	        return elapsed > DURATION;
	    }
	    
	    @Override
	    protected void update(float delta) {
	        elapsed += delta;
	    }
	    
	    @Override
	    public void render(float delta, OrthogonalTiledMapRenderer renderer) {
	    }
	}
	
	private static class PingIndicator implements TemporaryEntity {
	    private static final float SIZE = 1.5f;
	    private static final float OFFSET = 0.1f;
        private static final TextureRegion region = new TextureRegion(
                GameScreen.getTexture("sprite/indicator.png"));
        
	    private final Vector2 position = new Vector2();
        private final Vector2 direction = new Vector2();
        private final Agent target;
        private final Agent nearest;
        private boolean finished = false;
	    
	    public PingIndicator(Agent target, Agent nearest) {
	        this.target = target;
	        this.nearest = nearest;
	    }
	    
	    public void cancel() {
	        finished = true;
	    }
	    
        @Override
        public void update(float delta, Level level) {
        }

        @Override
        public void render(float delta, OrthogonalTiledMapRenderer renderer) {
            direction.set(nearest.getPosition()).sub(target.getPosition()).nor();
            position.set(target.getPosition()).add(direction.x * OFFSET, direction.y * OFFSET);
            float width = SIZE;
            float height = SIZE;
            
            Batch batch = renderer.getBatch();
            batch.begin();
            batch.draw(region,
                    position.x - width / 2, position.y - height / 2,  // position
                    width / 2, height / 2,  // origin
                    width, height,  // size
                    1f, 1f,  // scale
                    direction.angle());
            batch.end();  
        }

        @Override
        public float getZ() {
            return Float.POSITIVE_INFINITY;
        }
        
        @Override
        public boolean inOverlay() {
            return false;
        }

        @Override
        public Vector2 getPosition() {
            return target.getPosition();
        }

        @Override
        public boolean isFinished() {
            return finished;
        }

        @Override
        public void dispose() {
        }
	}
}
