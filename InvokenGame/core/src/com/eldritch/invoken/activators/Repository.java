package com.eldritch.invoken.activators;

import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.location.NaturalVector2;

public class Repository extends InteractableActivator {
    public Repository(NaturalVector2 position) {
        super(position, 1, 2);
    }

    @Override
    protected void onBeginInteraction(Agent interactor) {
        interactor.upload(true);
    }

    @Override
    protected void onEndInteraction(Agent interactor) {
        interactor.upload(false);
    }
}
