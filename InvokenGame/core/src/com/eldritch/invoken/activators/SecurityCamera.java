package com.eldritch.invoken.activators;

import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.eldritch.invoken.actor.GameCamera;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.location.NaturalVector2;

public class SecurityCamera extends ClickActivator implements GameCamera {
	private SecurityCamera next = null;
	
	public SecurityCamera(NaturalVector2 position) {
		super(NaturalVector2.of(position.x, position.y + 1));
	}

	@Override
	public void activate(Agent agent, Level level) {
		if (next != null && agent.usingRemoteCamera()) {
			agent.setCamera(next);
		} else {
			agent.resetCamera();
		}
	}

	@Override
	public void postRegister(Level level) {
		level.addSecurityCamera(this);
	}
	
	public void setNext(SecurityCamera camera) {
		this.next = camera;
	}

    @Override
    public void render(float delta, OrthogonalTiledMapRenderer renderer) {
        // delegated to layer
    }
}
