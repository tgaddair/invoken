package com.eldritch.invoken.actor.aug;

import com.eldritch.invoken.actor.Agent;
import com.eldritch.invoken.actor.Agent.Activity;
import com.eldritch.invoken.effects.Commanded;

public class Command extends Augmentation {
	@Override
	public Action getAction(Agent owner, Agent target) {
		return new CommandAction(owner, target);
	}
	
	@Override
	public boolean isValid(Agent owner, Agent target) {
		return target != null && target != owner && target.isAlive();
	}
	
	public class CommandAction extends AnimatedAction {
		private final Agent target;
		
		public CommandAction(Agent actor, Agent target) {
			super(actor, Activity.Swipe);
			this.target = target;
		}

		@Override
		public void apply() {
			target.addEffect(new Commanded(owner, target, 3));
		}
	}
}
