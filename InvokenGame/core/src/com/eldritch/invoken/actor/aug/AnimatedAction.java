package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.eldritch.invoken.actor.aug.Augmentation.AugmentationAction;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.encounter.Location;

public abstract class AnimatedAction extends AugmentationAction {
	final Activity activity;
	float stateTime = 0;
	boolean applied;
	
	public AnimatedAction(Agent actor, Activity activity, Augmentation aug) {
	    super(actor, aug);
		this.activity = activity;
		applied = false;
	}
	
	@Override
	public void update(float delta, Location location) {
		stateTime += delta;
		if (!applied && canApply()) {
		    apply(location);
		    applied = true;
		}
	}
	
	protected boolean canApply() {
	    Animation anim = owner.getAnimation(activity);
	    return anim.getKeyFrameIndex(stateTime) == anim.getKeyFrames().length / 3;
	}
	
	@Override
	public void render(OrthogonalTiledMapRenderer renderer) {
		// do nothing
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
