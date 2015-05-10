package com.eldritch.invoken.actor;

import com.eldritch.invoken.actor.type.Agent;

public interface AgentHandler {
    boolean handle(Agent agent);
    
    boolean handle(Object userData);
    
    short getCollisionMask();
}
