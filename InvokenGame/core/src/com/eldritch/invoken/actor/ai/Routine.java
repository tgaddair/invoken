package com.eldritch.invoken.actor.ai;

import com.eldritch.invoken.screens.GameScreen;

public interface Routine {
	boolean isFinished();
	
	boolean canInterrupt();
	
	boolean isValid();
	
	void takeAction(float delta, GameScreen screen);
}
