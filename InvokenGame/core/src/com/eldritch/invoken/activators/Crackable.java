package com.eldritch.invoken.activators;

import com.eldritch.invoken.actor.type.Agent;

public interface Crackable {
    void crack(Agent source);
    
    float getStrength();
}
