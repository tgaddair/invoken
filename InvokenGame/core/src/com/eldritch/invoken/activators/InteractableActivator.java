package com.eldritch.invoken.activators;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.util.Interactable;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.location.NaturalVector2;
import com.eldritch.invoken.screens.GameScreen;

public abstract class InteractableActivator extends ClickActivator implements Interactable {
    private static final Texture INDICATOR = GameScreen.getTexture("icon/indicator/interact.png");

    private Agent interactor = null;

    public InteractableActivator(NaturalVector2 position) {
        this(position, 1, 1);
    }

    public InteractableActivator(NaturalVector2 position, int width, int height) {
        this(position.x, position.y, width, height, new Vector2(position.x + width / 2f, position.y
                + height / 2f));
    }

    public InteractableActivator(float x, float y, int width, int height, Vector2 center) {
        this(x, y, width, height, ProximityParams.of(center));
    }

    public InteractableActivator(float x, float y, int width, int height, ProximityParams params) {
        super(x, y, width, height, params.withIndicator(new Indicator(INDICATOR, new Vector2(
                width / 2f, height))));
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
    protected boolean canActivate(Agent agent) {
        return super.canActivate(agent) && interactor == null;
    }

    @Override
    public void endInteraction() {
        onEndInteraction(interactor);
        interactor = null;
    }

    @Override
    public void postRegister(Level level) {
    }

    protected Agent getInteractor() {
        return interactor;
    }

    protected abstract void onBeginInteraction(Agent interactor);

    protected abstract void onEndInteraction(Agent interactor);
}
