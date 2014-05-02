package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

public interface Action {
	boolean isFinished();
	
	boolean isAnimated();
	
	void apply();
	
	void render(float delta, OrthogonalTiledMapRenderer renderer);
}
