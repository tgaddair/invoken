package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.eldritch.invoken.actor.aug.Augmentation.AugmentationAction;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.location.Level;

public abstract class AnimatedAction extends AugmentationAction {
	final Activity activity;
	final float timeScale;
	float elapsed = 0;
	float stateTime = 0;
	boolean applied = false;
	
	boolean canApply = false;
	float holdTime = 0;
	float postHoldTime = 0;
	
	public AnimatedAction(Agent agent, Activity activity, Augmentation aug) {
	    this(agent, activity, agent.getAttackSpeed(), aug);
	}
	
	public AnimatedAction(Agent agent, Activity activity, float timeScale, Augmentation aug) {
	    super(agent, aug);
        this.activity = activity;
        this.timeScale = timeScale;
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
		} else if (!isAnimationFinished()) {
	        stateTime += delta * getOwner().getInfo().getEfficacy() * timeScale;
		} else {
		    postHoldTime += delta;
		}
		
		if (!applied && canApply()) {
		    apply(level);
		    applied = true;
		}
	}
	
	protected float getHoldSeconds() {
	    return 0;
	}
	
	protected float getPostHoldSeconds() {
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
		return isAnimationFinished() && postHoldTime > getPostHoldSeconds();
	}
	
	@Override
	public boolean isAnimated() {
		return true;
	}
	
	private boolean isAnimationFinished() {
	    return owner.getAnimation(activity).isAnimationFinished(stateTime);
	}
}
