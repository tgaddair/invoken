package com.eldritch.invoken.activators;

import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.encounter.Location;

public interface Activator {
    boolean click(Agent agent, Location location, float x, float y);
    
    void activate(Agent agent, Location location);
}
