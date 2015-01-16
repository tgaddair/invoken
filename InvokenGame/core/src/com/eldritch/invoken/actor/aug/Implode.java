package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.effects.Imploding;
import com.eldritch.invoken.encounter.Location;

public class Implode extends Augmentation {
	private static final float RADIUS = 3;
	private static final float DURATION = 3;
	private static final float MAGNITUDE = 1000;
	
	private static class Holder {
        private static final Implode INSTANCE = new Implode();
	}
	
	public static Implode getInstance() {
		return Holder.INSTANCE;
	}
	
    private Implode() {
        super("implode", false);
    }
    
	@Override
	public boolean isValid(Agent owner, Agent target) {
		return owner.hasLineOfSight(target);
	}

	@Override
	public boolean isValid(Agent owner, Vector2 target) {
		return owner.hasLineOfSight(target);
	}

	@Override
	public Action getAction(Agent owner, Agent target) {
		return new ImplodeAction(owner, target.getPosition());
	}
	
	@Override
	public Action getAction(Agent owner, Vector2 target) {
		return new ImplodeAction(owner, target);
	}
	
	@Override
    public int getCost(Agent owner) {
        return 2;
    }
	
    @Override
    public float quality(Agent owner, Agent target, Location location) {
        return 1;
    }
	
	public class ImplodeAction extends AnimatedAction {
		private final Vector2 target;
		
		public ImplodeAction(Agent actor, Vector2 target) {
			super(actor, Activity.Swipe, Implode.this);
			this.target = target;
		}

		@Override
		public void apply(Location location) {
			Agent owner = getOwner();
			for (Agent neighbor : owner.getNeighbors()) {
				if (neighbor.inRange(target, RADIUS)) {
					neighbor.addEffect(new Imploding(neighbor, owner, target, DURATION, MAGNITUDE));
	    		}
        	}
		}
		
		@Override
        public Vector2 getPosition() {
            return owner.getPosition();
        }
	}
}
