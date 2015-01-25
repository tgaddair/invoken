package com.eldritch.invoken.effects;

import com.eldritch.invoken.actor.aug.Observe;
import com.eldritch.invoken.actor.type.Agent;

public class Observing extends ToggledEffect<Observe> {
    private final Agent observed;
    
	public Observing(Agent target, Agent observed, Observe aug, int cost) {
	    super(target, aug, Observe.class, cost);
	    this.observed = observed;
	}

	@Override
	protected void afterApply() {
	    target.setCamera(observed.getCamera());
	}

	@Override
	protected void afterDispel() {
		target.resetCamera();
	}
}
