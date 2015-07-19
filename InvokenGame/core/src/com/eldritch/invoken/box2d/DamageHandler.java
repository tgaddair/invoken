package com.eldritch.invoken.box2d;

import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.box2d.AgentHandler.DefaultAgentHandler;
import com.eldritch.invoken.util.Damager;

public abstract class DamageHandler extends DefaultAgentHandler {
    @Override
    public boolean handle(Agent agent) {
        return false;
    }

    @Override
    public boolean handle(Object userData) {
        if (userData instanceof Damager) {
            Damager damager = (Damager) userData;
            return handle(damager);
        }
        return false;
    }
    
    protected abstract boolean handle(Damager damager);
}
