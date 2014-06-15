package com.eldritch.invoken.effects;

import com.eldritch.invoken.actor.type.Agent;

public class Frenzied extends BasicEffect {
	private final float duration;
	
	public Frenzied(Agent master, Agent target, float duration) {
	    super(target);
		this.duration = duration;
	}

	@Override
	public boolean isFinished() {
		return getStateTime() > duration;
	}

	@Override
	public void dispel() {
	    target.setConfused(false);
	}
	
	@Override
    protected void doApply() {
        target.setConfused(true);
    }

    @Override
    protected void update(float delta) {
    }
}
