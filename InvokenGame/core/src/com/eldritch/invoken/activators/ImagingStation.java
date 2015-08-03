package com.eldritch.invoken.activators;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.util.Backup;
import com.eldritch.invoken.location.NaturalVector2;

public class ImagingStation extends InteractableActivator {
    private static final int WIDTH = 3;
    private static final int HEIGHT = 2;
    private static final float RADIUS = 2f;

    public ImagingStation(NaturalVector2 position) {
        super(position.x, position.y, WIDTH, HEIGHT, ProximityParams.of(new Vector2(
                position.x + WIDTH / 2f, position.y + HEIGHT / 2f), Vector2.Zero, RADIUS));
    }

    @Override
    protected void onBeginInteraction(Agent interactor) {
        interactor.toggleOn(Backup.class);
    }

    @Override
    protected void onEndInteraction(Agent interactor) {
        interactor.toggleOff(Backup.class);
    }

    @Override
    public float getZ() {
        // do this so the indicator appears on top
        return Float.NEGATIVE_INFINITY;
    }
}
