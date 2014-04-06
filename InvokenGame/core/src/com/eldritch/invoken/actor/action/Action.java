package com.eldritch.invoken.actor.action;

public interface Action {
	boolean isFinished();
	
	void apply();
}
