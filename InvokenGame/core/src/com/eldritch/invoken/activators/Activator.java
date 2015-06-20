package com.eldritch.invoken.activators;

import com.eldritch.invoken.actor.Drawable;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.location.Level;

public interface Activator extends Drawable {
    void update(float delta, Level level);
    
    boolean click(Agent agent, Level level, float x, float y);
    
    void activate(Agent agent, Level level);
    
    void register(Level level);
}
