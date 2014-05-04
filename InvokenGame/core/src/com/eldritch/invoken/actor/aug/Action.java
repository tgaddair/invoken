package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.eldritch.invoken.actor.Agent.Activity;

public interface Action {
	boolean isFinished();
	
	boolean isAnimated();
	
	void apply();
	
	void update(float delta);
	
	void render(OrthogonalTiledMapRenderer renderer);
	
	float getStateTime();
	
	Activity getActivity();
}
