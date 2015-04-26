package com.eldritch.invoken.actor.type;

import com.badlogic.gdx.ai.steer.behaviors.Wander;
import com.badlogic.gdx.ai.steer.limiters.LinearAccelerationLimiter;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.Profession;
import com.eldritch.invoken.location.Location;
import com.eldritch.invoken.util.Settings;

public class DummyPlayer extends Player {
    public DummyPlayer(Profession profession, int level, float x, float y, Location location,
            String body) {
        super(profession, level, x, y, location, body);

        Wander<Vector2> wander = new Wander<Vector2>(this).setFaceEnabled(false)
                .setLimiter(new LinearAccelerationLimiter(5)).setWanderOffset(2)
                .setWanderOrientation(0).setWanderRadius(0.5f).setWanderRate(MathUtils.PI / 5);
        setBehavior(wander);

        setCollisionMask(Settings.BIT_PERIMETER);
    }

    @Override
    protected void takeAction(float delta, Location screen) {
        // steering and movement
        if (steeringBehavior != null) {
            // Calculate steering acceleration
            steeringBehavior.calculateSteering(steeringOutput);

            // Apply steering acceleration to move this agent
            applySteering(steeringOutput, delta);
        }
    }
    
    @Override
    public void render(float delta, OrthogonalTiledMapRenderer renderer) {
    }

    @Override
    protected float damage(float value) {
        return 0;
    }

    @Override
    public boolean isVisible() {
        return false;
    }
}
