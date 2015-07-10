package com.eldritch.invoken.activators;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.location.NaturalVector2;

public class ResearchStation extends InteractableActivator {
    private static final int X = 1;
    private static final int Y = 1;
    private static final int WIDTH = 2;
    private static final int HEIGHT = 3;
    
    private final Vector2 center;
    
    public ResearchStation(NaturalVector2 position) {
        super(NaturalVector2.of(position.x + X, position.y + Y), WIDTH, HEIGHT);
        
        Vector2 worldPosition = getPosition();
        center = new Vector2(worldPosition.x + WIDTH / 2f, worldPosition.y + HEIGHT / 2f);
    }
    
    @Override
    protected void onBeginInteraction(Agent interactor) {
        interactor.research(true);
    }

    @Override
    protected void onEndInteraction(Agent interactor) {
        interactor.research(false);
    }
    
    @Override
    protected Vector2 getCenter() {
        return center;
    }
}
