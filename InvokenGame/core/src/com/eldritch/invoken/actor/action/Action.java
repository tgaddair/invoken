package com.eldritch.invoken.actor.action;

import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

public interface Action {
	boolean isFinished();
	
	void apply();
	
	void render(float delta, OrthogonalTiledMapRenderer renderer);
}
