package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.aug.Augmentation.Target;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.effects.Resurrected;
import com.eldritch.invoken.encounter.Location;

public class Resurrect extends Augmentation {
	private static final int BASE_COST = 30;
	
	private static class Holder {
        private static final Resurrect INSTANCE = new Resurrect();
	}
	
	public static Resurrect getInstance() {
		return Holder.INSTANCE;
	}
	
    private Resurrect() {
        super("resurrect");
    }
    
    @Override
    public Action getBestAction(Agent owner, Agent target) {
        // TODO: look nearby
        return getAction(owner, target);
    }
    
	@Override
	public Action getAction(Agent owner, Agent target) {
		return new ResurrectAction(owner, target);
	}
	
	@Override
    public Action getAction(Agent owner, Vector2 target) {
        return getAction(owner, owner.getTarget());
    }
	
	@Override
	public boolean isValid(Agent owner, Agent target) {
		return target != null && target != owner && !target.isAlive() 
				&& owner.hasLineOfSight(target);
	}
	
	@Override
    public boolean isValid(Agent owner, Vector2 target) {
        return isValid(owner, owner.getTarget());
    }
	
	@Override
    public int getCost(Agent owner) {
        return BASE_COST;
    }
	
	@Override
    public float quality(Agent owner, Agent target, Location location) {
        return 5;
    }
	
	@Override
	protected void setBestTarget(Agent agent, Agent goal, Target target) {
	    for (Agent neighbor : goal.getNeighbors()) {
	        if (!neighbor.isAlive()) {
	            target.set(neighbor);
	        }
	    }
    }
	
	public class ResurrectAction extends AnimatedAction {
		private final Agent target;
		
		public ResurrectAction(Agent actor, Agent target) {
			super(actor, Activity.Cast, Resurrect.this);
			this.target = target;
		}

		@Override
		public void apply(Location location) {
			if (!target.isAlive()) {
				target.addEffect(new Resurrected(owner, target, BASE_COST));
			}
		}
		
		@Override
        public Vector2 getPosition() {
            return target.getPosition();
        }
	}
}
