package com.eldritch.invoken.actor.aug;

import com.eldritch.invoken.actor.Agent;
import com.eldritch.invoken.actor.Agent.Activity;
import com.eldritch.invoken.effects.Frenzied;

public class Frenzy extends Augmentation {
    public Frenzy() {
        super("frenzy");
    }
    
	@Override
	public Action getAction(Agent owner, Agent target) {
		return new FrenzyAction(owner, target);
	}
	
	@Override
	public boolean isValid(Agent owner, Agent target) {
		return target != null && target != owner && target.isAlive();
	}
	
	public class FrenzyAction extends AnimatedAction {
		private final Agent target;
		
		public FrenzyAction(Agent actor, Agent target) {
			super(actor, Activity.Swipe);
			this.target = target;
		}

		@Override
		public void apply() {
			target.addEffect(new Frenzied(owner, target, 3));
		}
	}
}
