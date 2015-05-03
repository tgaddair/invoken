package com.eldritch.invoken.activators;

import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.util.Interactable;
import com.eldritch.invoken.location.Location;
import com.eldritch.invoken.location.NaturalVector2;

public class Repository extends ClickActivator implements Interactable {
    private Agent interactor = null;

    public Repository(NaturalVector2 position) {
        super(position, 1, 2);
    }

    @Override
    public void activate(Agent agent, Location location) {
        // mutual exclusion
        if (interactor == null) {
            // assign the interactor
            interactor = agent;
            interactor.beginInteraction(this);
            interactor.upload(true);
        }
    }

    @Override
    public void register(Location location) {
    }

    @Override
    public void render(float delta, OrthogonalTiledMapRenderer renderer) {
        // delegated to layer
    }

    @Override
    public boolean canInteract() {
        // distance constraint already handled by the parent class
        return true;
    }

    @Override
    public void endInteraction() {
        interactor.upload(false);
        interactor = null;
    }
}
