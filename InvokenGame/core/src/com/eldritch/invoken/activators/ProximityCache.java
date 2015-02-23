package com.eldritch.invoken.activators;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.encounter.NaturalVector2;

public class ProximityCache {
    private final Map<Agent, LastProximity> cache = new HashMap<Agent, LastProximity>();
    private final float maxDistance;
    
    public ProximityCache(float maxDistance) {
        this.maxDistance = maxDistance;
    }

    public boolean inProximity(Vector2 center, Agent agent) {
        NaturalVector2 position = agent.getNaturalPosition();
        if (!cache.containsKey(agent) || position != cache.get(agent).lastPosition) {
            cache.put(agent, new LastProximity(position,
                    agent.getPosition().dst2(center) < maxDistance));
        }
        return cache.get(agent).inProximity;
    }

    private static class LastProximity {
        private final NaturalVector2 lastPosition;
        private final boolean inProximity;

        public LastProximity(NaturalVector2 position, boolean proxmity) {
            this.lastPosition = position;
            this.inProximity = proxmity;
        }
    }
}
