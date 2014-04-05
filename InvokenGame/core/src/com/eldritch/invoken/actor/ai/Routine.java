package com.eldritch.invoken.actor.ai;

public interface Routine {
	boolean isFinished();
	
	boolean isValid();
	
	void takeAction(float delta);
}
