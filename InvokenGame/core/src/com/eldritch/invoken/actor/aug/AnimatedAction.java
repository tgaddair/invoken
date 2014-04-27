package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
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
	public void render(float delta, OrthogonalTiledMapRenderer renderer) {
		stateTime += delta;
		TextureRegion frame = owner.getAnimation(activity).getKeyFrame(stateTime);
		owner.render(frame, renderer);
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
