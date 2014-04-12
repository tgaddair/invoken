package com.eldritch.invoken.actor.aug;

import com.eldritch.invoken.actor.Agent;
import com.eldritch.invoken.actor.action.Action;

public abstract class Augmentation {
	private int slots;
	private int uses;
	
	public void invoke(Agent owner, Agent target) {
		if (target != null && target != owner) {
			owner.addAction(getAction(owner, target));
		}
	}
	
	public abstract Action getAction(Agent owner, Agent target);
}
