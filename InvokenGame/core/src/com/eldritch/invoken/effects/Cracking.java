package com.eldritch.invoken.effects;

import com.eldritch.invoken.actor.aug.Crack;
import com.eldritch.invoken.actor.type.Agent;

public class Cracking extends ToggledEffect<Crack> {
	public Cracking(Agent target, Crack aug, int cost) {
	    super(target, aug, Crack.class, cost);
	}

	@Override
	protected void afterApply() {
	}

	@Override
	protected void afterDispel() {
		target.resetCamera();
	}
}
