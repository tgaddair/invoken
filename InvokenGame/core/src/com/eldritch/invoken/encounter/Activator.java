package com.eldritch.invoken.encounter;

import com.eldritch.invoken.actor.type.Agent;

public interface Activator {
    boolean click(Agent agent, Location location, float x, float y);
    
    void activate(Agent agent, Location location);
}
