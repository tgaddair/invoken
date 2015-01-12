package com.eldritch.invoken.actor.aug;

import java.util.HashSet;
import java.util.Set;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.actor.type.AoeProjectile;
import com.eldritch.invoken.effects.Infected;
import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.screens.GameScreen;

public class Infect extends Augmentation {
	private static class Holder {
        private static final Infect INSTANCE = new Infect();
	}
	
	public static Infect getInstance() {
		return Holder.INSTANCE;
	}
	
    private Infect() {
        super("infect");
    }

    @Override
    public Action getAction(Agent owner, Agent target) {
    	return getAction(owner, target.getPosition());
    }
    
    @Override
    public Action getAction(Agent owner, Vector2 target) {
        return new InfectAction(owner, target);
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

    public class InfectAction extends AnimatedAction {
        private final Vector2 target;

        public InfectAction(Agent actor, Vector2 target) {
            super(actor, Activity.Swipe, Infect.this);
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
    
    public static class Grenade extends AoeProjectile {
        private static final TextureRegion texture = new TextureRegion(
                GameScreen.getTexture("sprite/effects/grenade.png"));
        private static final TextureRegion[] explosionRegions = GameScreen.getMergedRegion(
        		"sprite/effects/infect_cloud.png", 256, 256);
        
        public Grenade() {
            super(texture, explosionRegions, 5, 5, 2);
        }
		
        @Override
		protected void onDetonate() {
		}

		@Override
		protected void doDuringExplosion(float delta, Location location) {
			// create a set of immune agents to prevent the infinite spreading loop
			// once you've gotten infected by this virus, you can't get it again
			Set<Agent> immune = new HashSet<Agent>();
			
			// no friendly fire
			Agent owner = getOwner();
			immune.add(owner);
			
			for (Agent neighbor : owner.getNeighbors()) {
				if (!neighbor.isToggled(Infected.class)) {
					// infection does not stack
					if (neighbor.inRange(getPosition(), getRadius())) {
						immune.add(neighbor);
						neighbor.addEffect(new Infected(
								owner, neighbor, immune, getDamage(neighbor), 3, getRadius()));
		    		}
				}
        	}
		}

		@Override
		protected void free() {
			bulletPool.free(this);
		}
    }

    private static Pool<Grenade> bulletPool = new Pool<Grenade>() {
        @Override
        protected Grenade newObject() {
            return new Grenade();
        }
    };
}
