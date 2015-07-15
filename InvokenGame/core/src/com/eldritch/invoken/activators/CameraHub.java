package com.eldritch.invoken.activators;

import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.eldritch.invoken.actor.aug.Crack;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.location.NaturalVector2;
import com.eldritch.invoken.screens.GameScreen;

public class CameraHub extends ClickActivator {
	public CameraHub(NaturalVector2 position) {
		super(position);
	}

	@Override
	public void activate(Agent agent, Level level) {
		if (agent.isToggled(Crack.class) && level.hasSecurityCamera()) {
			agent.setCamera(level.getFirstSecurityCamera());
		} else {
		    GameScreen.toast("Requires: Crack Augmentation");
		}
	}

	@Override
	public void postRegister(Level level) {
	}

    @Override
    public void render(float delta, OrthogonalTiledMapRenderer renderer) {
        // delegated to layer
    }
}
