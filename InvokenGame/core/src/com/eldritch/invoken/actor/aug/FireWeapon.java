package com.eldritch.invoken.actor.aug;

import com.eldritch.invoken.actor.Agent;
import com.eldritch.invoken.actor.action.Action;
import com.eldritch.invoken.actor.action.Fire;

public class FireWeapon extends Augmentation {
	@Override
	public Action getAction(Agent owner, Agent target) {
		return new Fire(owner, target);
	}
}
