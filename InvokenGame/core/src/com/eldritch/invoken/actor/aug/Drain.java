package com.eldritch.invoken.actor.aug;

import com.eldritch.invoken.actor.Agent;
import com.eldritch.invoken.actor.Agent.Activity;
import com.eldritch.invoken.effects.Draining;

public class Drain extends Augmentation {
	@Override
	public Action getAction(Agent owner, Agent target) {
		return new DrainAction(owner, target);
	}
	
	@Override
	public boolean isValid(Agent owner, Agent target) {
		return target != null && target != owner && target.isAlive();
	}
	
	public class DrainAction extends AnimatedAction {
		private final Agent target;
		
		public DrainAction(Agent actor, Agent target) {
			super(actor, Activity.Swipe);
			this.target = target;
		}

		@Override
		public void apply() {
			target.addEffect(new Draining(owner, target, 5, 2));
		}
	}
}
