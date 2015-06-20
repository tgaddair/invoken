package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.eldritch.invoken.actor.aug.Augmentation.AugmentationAction;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.location.Level;

public abstract class AnimatedAction extends AugmentationAction {
	final Activity activity;
	float elapsed = 0;
	float stateTime = 0;
	boolean applied = false;
	
	boolean canApply = false;
	float holdTime = 0;
	
	public AnimatedAction(Agent actor, Activity activity, Augmentation aug) {
	    super(actor, aug);
		this.activity = activity;
		applied = false;
	}
	
	@Override
	public void update(float delta, Level level) {
	    elapsed += delta;
		if (!canApply && canApplyFrame()) {
		    holdTime += delta;
		    if (holdTime > getHoldSeconds()) {
		        canApply = true;
		    }
		} else {
	        stateTime += delta * getOwner().getInfo().getEfficacy();
		}
		
		if (!applied && canApply()) {
		    apply(level);
		    applied = true;
		}
	}
	
	protected float getHoldSeconds() {
	    return 0;
	}
	
	protected boolean canApplyFrame() {
	    Animation anim = owner.getAnimation(activity);
        return anim.getKeyFrameIndex(stateTime) == anim.getKeyFrames().length / 2;
	}
	
	protected boolean canApply() {
	    return canApply;
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
}
