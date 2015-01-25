package com.eldritch.invoken.effects;

import com.eldritch.invoken.actor.aug.Observe;
import com.eldritch.invoken.actor.type.Agent;

public class Observing extends ToggledEffect<Observe> {
    private final Agent owner;
    
	public Observing(Agent owner, Agent target, Observe aug, int cost) {
	    super(target, aug, Observe.class, cost);
	    this.owner = owner;
	}

	@Override
	protected void afterApply() {
	    owner.setCamera(target.getCamera());
	}

	@Override
	protected void afterDispel() {
		owner.resetCamera();
	}
}
