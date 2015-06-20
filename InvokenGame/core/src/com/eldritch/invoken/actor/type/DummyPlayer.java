package com.eldritch.invoken.actor.type;

import java.util.List;

import com.badlogic.gdx.ai.steer.behaviors.Pursue;
import com.badlogic.gdx.ai.steer.limiters.LinearAccelerationLimiter;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.Profession;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.util.Settings;

public class DummyPlayer extends Player {
    private static final float MIN_DST2 = 9f;
    private static final float DURATION = 20f;
    
    private final NavigatedSteerable lastSeen;
    private final Pursue<Vector2> pursue;
    private float elapsed = 0;
    
    public DummyPlayer(Profession profession, int level, float x, float y, Level location,
            String body) {
        super(profession, level, x, y, location, body);
        lastSeen = new NavigatedSteerable(this, location);

        pursue = new Pursue<Vector2>(this, this).setLimiter(new LinearAccelerationLimiter(7));
        setBehavior(pursue);

        setCollisionMask(Settings.BIT_PERIMETER);
    }

    @Override
    protected void takeAction(float delta, Level level) {
        elapsed += delta;
        if (pursue.getTarget() == this || dst2(lastSeen.getTarget()) < MIN_DST2 || elapsed > DURATION) {
            List<Agent> agents = level.getAllAgents();
            Agent agent = agents.get((int) (Math.random() * agents.size()));
            lastSeen.setPosition(agent);
            pursue.setTarget(lastSeen);
            elapsed = 0;
        }
        
        // steering and movement
        lastSeen.update(delta);
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
