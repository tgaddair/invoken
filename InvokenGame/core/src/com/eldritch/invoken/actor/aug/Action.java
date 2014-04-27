package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.eldritch.invoken.actor.Agent;
import com.eldritch.invoken.actor.Agent.Activity;

public interface Action {
	boolean isFinished();
	
	boolean isAnimated();
	
	void apply();
	
	void render(float delta, OrthogonalTiledMapRenderer renderer);
}
