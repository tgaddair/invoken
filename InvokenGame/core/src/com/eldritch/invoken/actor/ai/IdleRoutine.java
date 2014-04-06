package com.eldritch.invoken.actor.ai;

import com.eldritch.invoken.actor.AnimatedEntity;

public class IdleRoutine implements Routine {
	private final AnimatedEntity actor;
	private final int duration;
	private float elapsed = 0;
	
	public IdleRoutine(AnimatedEntity actor) {
		this.actor = actor;
		this.duration = 3;
	}
	
	@Override
	public boolean isFinished() {
		return elapsed >= duration;
	}
	
	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public void takeAction(float delta) {
		if (isFinished()) {
			elapsed = 0;
		}
		elapsed += delta;
	}
}
