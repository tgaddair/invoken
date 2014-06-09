package com.eldritch.invoken.actor.aug;

import com.eldritch.invoken.actor.Agent;
import com.eldritch.invoken.actor.Agent.Activity;
import com.eldritch.invoken.encounter.Location;

public class Resurrect extends Augmentation {
    public Resurrect() {
        super("resurrect");
    }
    
	@Override
	public Action getAction(Agent owner, Agent target) {
		return new ResurrectAction(owner, target);
	}
	
	@Override
	public boolean isValid(Agent owner, Agent target) {
		return target != null && target != owner && !target.isAlive();
	}
	
	@Override
    public float quality(Agent owner, Agent target, Location location) {
        return 1;
    }
	
	public class ResurrectAction extends AnimatedAction {
		private final Agent target;
		
		public ResurrectAction(Agent actor, Agent target) {
			super(actor, Activity.Cast);
			this.target = target;
		}

		@Override
		public void apply(Location location) {
			if (!target.isAlive()) {
				target.resurrect();
				owner.addFollower(target);
			}
		}
	}
}
