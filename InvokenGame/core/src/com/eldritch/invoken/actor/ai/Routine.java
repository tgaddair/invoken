package com.eldritch.invoken.actor.ai;

public interface Routine {
	boolean isValid();
	
	void takeAction(float delta);
}
