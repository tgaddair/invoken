package com.eldritch.invoken.actor.ai;

import com.eldritch.invoken.encounter.Location;

public interface Routine {
	boolean isFinished();
	
	boolean canInterrupt();
	
	boolean isValid();
	
	void reset();
	
	void takeAction(float delta, Location screen);
}
