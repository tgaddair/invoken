package com.eldritch.invoken.effects;

import com.eldritch.invoken.actor.aug.Scramble;
import com.eldritch.invoken.actor.type.Agent;

public class Scrambled extends ActivatedEffect<Scramble> {
	private final Agent target;
	
	public Scrambled(Agent owner, Agent target, Scramble aug, int cost) {
	    super(owner, aug, Scramble.class, cost);
	    this.target = target;
	}

	@Override
	protected void afterApply() {
		System.out.println("scramble");
		target.setConfused(true);
	}

	@Override
	protected void afterDispel() {
		System.out.println("dispel");
		target.setConfused(false);
	}
}
