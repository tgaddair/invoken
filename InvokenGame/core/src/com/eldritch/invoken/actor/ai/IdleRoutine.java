package com.eldritch.invoken.actor.ai;

import com.eldritch.invoken.actor.Agent;
import com.eldritch.invoken.screens.GameScreen;

public class IdleRoutine implements Routine {
	private final Agent actor;
	private final int duration;
	private float elapsed = 0;
	
	public IdleRoutine(Agent actor) {
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
	public void takeAction(float delta, GameScreen screen) {
		if (isFinished()) {
			elapsed = 0;
		}
		elapsed += delta;
	}
}