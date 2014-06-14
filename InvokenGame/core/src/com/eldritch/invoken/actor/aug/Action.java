package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.Agent.Activity;
import com.eldritch.invoken.encounter.Location;

public interface Action {
	boolean isFinished();
	
	boolean isAnimated();
	
	void apply(Location location);
	
	void update(float delta, Location location);
	
	void render(OrthogonalTiledMapRenderer renderer);
	
	float getStateTime();
	
	Activity getActivity();
	
	Vector2 getPosition();
}
