package com.eldritch.invoken.actor.aug;

import com.eldritch.invoken.actor.Action;
import com.eldritch.invoken.actor.Agent;

public abstract class Augmentation {
	private int slots;
	private int uses;
	
	public void invoke(Agent owner, Agent target) {
		if (isValid(owner, target)) {
			owner.addAction(getAction(owner, target));
		}
	}
	
	public abstract boolean isValid(Agent owner, Agent target);
	
	public abstract Action getAction(Agent owner, Agent target);
}
