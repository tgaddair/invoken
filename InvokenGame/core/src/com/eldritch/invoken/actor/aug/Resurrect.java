package com.eldritch.invoken.actor.aug;

import com.eldritch.invoken.actor.Agent;
import com.eldritch.invoken.actor.Agent.Activity;

public class Resurrect extends Augmentation {
	@Override
	public Action getAction(Agent owner, Agent target) {
		return new ResurrectAction(owner, target);
	}
	
	@Override
	public boolean isValid(Agent owner, Agent target) {
		return target != null && target != owner && !target.isAlive();
	}
	
	public class ResurrectAction extends AnimatedAction {
		private final Agent target;
		
		public ResurrectAction(Agent actor, Agent target) {
			super(actor, Activity.Cast);
			this.target = target;
		}

		@Override
		public void apply() {
			if (!target.isAlive()) {
				target.resurrect();
				owner.addFollower(target);
			}
		}
	}
}
