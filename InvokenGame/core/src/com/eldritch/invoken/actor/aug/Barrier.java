package com.eldritch.invoken.actor.aug;

import com.eldritch.invoken.actor.Agent;
import com.eldritch.invoken.actor.Agent.Activity;
import com.eldritch.invoken.effects.Shield;

public class Barrier extends Augmentation {
    public Barrier() {
        super("barrier");
    }
    
	@Override
	public Action getAction(Agent owner, Agent target) {
		return new ShieldAction(owner);
	}
	
	@Override
	public boolean isValid(Agent owner, Agent target) {
		return true;
	}
	
	public class ShieldAction extends AnimatedAction {
		public ShieldAction(Agent actor) {
			super(actor, Activity.Cast);
		}

		@Override
		public void apply() {
			if (owner.toggle(Shield.class)) {
				owner.addEffect(new Shield(owner));
			}
		}
	}
}
