package com.eldritch.invoken.activators;

import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.location.Location;
import com.eldritch.invoken.location.NaturalVector2;
import com.eldritch.invoken.screens.GameScreen;

public class Repository extends ClickActivator {
	public Repository(NaturalVector2 position) {
		super(position, 1, 2);
	}

	@Override
	public void activate(Agent agent, Location location) {
	    GameScreen.toast("Saving...");
	    GameScreen.save(location);
	}

	@Override
	public void register(Location location) {
	}

    @Override
    public void render(float delta, OrthogonalTiledMapRenderer renderer) {
        // delegated to layer
    }
}
