package com.eldritch.invoken.actor.aug;

import com.eldritch.invoken.actor.Agent;
import com.eldritch.invoken.actor.action.Action;
import com.eldritch.invoken.actor.action.ResurrectAction;

public class Resurrect extends Augmentation {
	@Override
	public Action getAction(Agent owner, Agent target) {
		return new ResurrectAction(owner, target);
	}
}
