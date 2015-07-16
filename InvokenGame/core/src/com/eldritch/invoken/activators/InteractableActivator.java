package com.eldritch.invoken.activators;

import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.util.Interactable;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.location.NaturalVector2;

public abstract class InteractableActivator extends ClickActivator implements Interactable {
    private Agent interactor = null;

    public InteractableActivator(NaturalVector2 position) {
        super(position);
    }

    public InteractableActivator(NaturalVector2 position, int width, int height) {
        super(position, width, height);
    }
    
    public InteractableActivator(float x, float y, int width, int height, Vector2 center) {
        this(x, y, width, height, ProximityParams.of(center));
    }
    
    public InteractableActivator(float x, float y, int width, int height, ProximityParams params) {
        super(x, y, width, height, params);
    }

    @Override
    public void activate(Agent agent, Level level) {
        // mutual exclusion
        if (interactor == null) {
            // assign the interactor
            interactor = agent;
            interactor.beginInteraction(this);
            onBeginInteraction(interactor);
        }
    }

    @Override
    public boolean canInteract() {
        // distance constraint already handled by the parent class
        return true;
    }

    @Override
    public void endInteraction() {
        onEndInteraction(interactor);
        interactor = null;
    }
    
    @Override
    public void postRegister(Level level) {
    }

    @Override
    public void render(float delta, OrthogonalTiledMapRenderer renderer) {
        // delegated to layer
    }
    
    protected abstract void onBeginInteraction(Agent interactor);
    
    protected abstract void onEndInteraction(Agent interactor);
}
