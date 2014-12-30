package com.eldritch.invoken.effects;

import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.eldritch.invoken.actor.type.Agent;

public interface Effect {
	boolean isFinished();
	
	void apply(float delta);
	
	void dispel();
	
	void render(float delta, OrthogonalTiledMapRenderer renderer);
}
