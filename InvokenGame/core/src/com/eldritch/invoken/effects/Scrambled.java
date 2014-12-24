package com.eldritch.invoken.effects;

import com.eldritch.invoken.actor.aug.Scramble;
import com.eldritch.invoken.actor.type.Agent;

public class Scrambled extends ToggledEffect<Scramble> {
	private final Agent target;
	
	public Scrambled(Agent owner, Agent target, Scramble aug, int cost) {
	    super(owner, aug, Scramble.class, cost);
	    this.target = target;
	}

	@Override
	protected void afterApply() {
		target.setConfused(true);
	}

	@Override
	protected void afterDispel() {
		target.setConfused(false);
	}
}
