package com.eldritch.invoken.actor.aug;

import com.eldritch.invoken.actor.Agent.Activity;

public interface Action {
	boolean isFinished();
	
	boolean isAnimated();
	
	void apply();
	
	void update(float delta);
	
	float getStateTime();
	
	Activity getActivity();
}
