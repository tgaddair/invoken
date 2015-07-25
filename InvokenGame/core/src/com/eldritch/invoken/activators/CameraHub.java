package com.eldritch.invoken.activators;

import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.location.NaturalVector2;
import com.eldritch.invoken.screens.GameScreen;

public class CameraHub extends InteractableActivator {
	public CameraHub(NaturalVector2 position) {
		super(position, 2, 2);
	}

	@Override
	public void postRegister(Level level) {
	}

    @Override
    protected void onBeginInteraction(Agent interactor) {
        Level level = interactor.getLocation();
        if (level.hasSecurityCamera()) {
            interactor.setCamera(level.getFirstSecurityCamera());
        } else {
            GameScreen.toast("Security System Offline");
        }
    }

    @Override
    protected void onEndInteraction(Agent interactor) {
    }
}
