package com.eldritch.invoken.effects;

import com.eldritch.invoken.actor.type.Agent;

public class Regenerating extends BasicEffect {
    private final float magnitude;
    private final float duration;
    private float elapsed = 0;
	private boolean applied = false;
	
	public Regenerating(Agent target, float magnitude, float duration) {
	    super(target);
	    this.magnitude = magnitude;
	    this.duration = duration;
	}

	@Override
	public boolean isFinished() {
		return applied || elapsed > duration || !target.isAlive();
	}

	@Override
	public void dispel() {
	}
	
	@Override
    protected void doApply() {
	    if (duration == 0) {
	        target.heal(magnitude);
	        applied = true;
	    }
    }

    @Override
    protected void update(float delta) {
        if (duration > 0) {
            elapsed += delta;
            target.heal(magnitude * delta);
        }
    }
}
