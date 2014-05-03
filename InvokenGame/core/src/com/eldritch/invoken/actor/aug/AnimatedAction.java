package com.eldritch.invoken.actor.aug;

import com.eldritch.invoken.actor.Agent;
import com.eldritch.invoken.actor.Agent.Activity;

public abstract class AnimatedAction implements Action {
	final Agent owner;
	final Activity activity;
	float stateTime = 0;
	
	public AnimatedAction(Agent actor, Activity activity) {
		this.owner = actor;
		this.activity = activity;
	}
	
	@Override
	public void update(float delta) {
		stateTime += delta;
	}
	
	@Override
	public float getStateTime() {
		return stateTime;
	}
	
	@Override
	public Activity getActivity() {
		return activity;
	}

	@Override
	public boolean isFinished() {
		return owner.getAnimation(activity).isAnimationFinished(stateTime);
	}
	
	@Override
	public boolean isAnimated() {
		return true;
	}
	
	// TODO: implement apply method, which applies a list of effects
	// allow Augmentation proto to specify which Action type it uses for its animation
}
