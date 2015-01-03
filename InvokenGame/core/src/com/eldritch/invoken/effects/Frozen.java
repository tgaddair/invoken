package com.eldritch.invoken.effects;

import com.eldritch.invoken.actor.type.Agent;

public class Frozen extends BasicEffect {
	private final float magnitude;
	private final float duration;
	
	public Frozen(Agent master, Agent target, float magnitude, float duration) {
	    super(target);
	    this.magnitude = magnitude;
		this.duration = duration;
	}

	@Override
	public boolean isFinished() {
		return getStateTime() > duration;
	}

	@Override
	public void dispel() {
		target.freeze(-magnitude);
	}
	
	@Override
    protected void doApply() {
		target.freeze(magnitude);
    }

    @Override
    protected void update(float delta) {
    }
}
