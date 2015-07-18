package com.eldritch.invoken.activators;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.location.NaturalVector2;

public class ResearchStation extends InteractableActivator {
    private static final int X = 1;
    private static final int Y = 1;
    private static final int WIDTH = 2;
    private static final int HEIGHT = 3;
    private static final float RADIUS = 2f;

    public ResearchStation(NaturalVector2 position) {
        super(position.x + X, position.y + Y, WIDTH, HEIGHT, ProximityParams.of(new Vector2(
                position.x + X + WIDTH / 2f, position.y + Y + HEIGHT / 2f), Vector2.Zero, RADIUS));
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
    public float getZ() {
        // do this so the indicator appears on top
        return Float.NEGATIVE_INFINITY;
    }
}
