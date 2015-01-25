package com.eldritch.invoken.activators;

import com.eldritch.invoken.actor.Drawable;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.encounter.Location;

public interface Activator extends Drawable {
    boolean click(Agent agent, Location location, float x, float y);
    
    void activate(Agent agent, Location location);
    
    void register(Location location);
}
