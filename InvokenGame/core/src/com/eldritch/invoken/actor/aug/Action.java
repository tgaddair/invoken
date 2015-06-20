package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.location.Level;

public interface Action {
	boolean isFinished();
	
	boolean isAnimated();
	
	void apply(Level level);
	
	void update(float delta, Level level);
	
	void render(OrthogonalTiledMapRenderer renderer);
	
	int getCost();
	
	float getStateTime();
	
	Activity getActivity();
	
	Vector2 getPosition();
}
