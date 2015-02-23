package com.eldritch.invoken.activators;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.encounter.NaturalVector2;

public class Teleporter extends BasicActivator implements ProximityActivator {
    private final ProximityCache proximityCache = new ProximityCache(1);
    private final String destination;
    private final Vector2 center;
    
    public Teleporter(NaturalVector2 position, String destination) {
        super(position);
        this.destination = destination;
        center = new Vector2(position.x, position.y);
    }

    @Override
    public void update(float delta, Location location) {
        if (inProximity(location.getPlayer())) {
            activate(location.getPlayer(), location);
        }
    }

    @Override
    public void activate(Agent agent, Location location) {
        location.transition(destination);
    }

    @Override
    public boolean inProximity(Agent agent) {
        return proximityCache.inProximity(center, agent);
    }
}
