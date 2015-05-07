package com.eldritch.invoken.location;

import com.eldritch.invoken.actor.AgentHandler;
import com.eldritch.invoken.actor.type.Agent;


public class Wall implements AgentHandler {
    @Override
    public boolean handle(Agent agent) {
        return false;
    }

    @Override
    public boolean handle(Object userData) {
        return false;
    }
}
