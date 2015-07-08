package com.eldritch.invoken.actor;

import com.eldritch.invoken.actor.AgentHandler.DefaultAgentHandler;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.location.Bullet;

public abstract class BulletHandler extends DefaultAgentHandler {
    @Override
    public boolean handle(Agent agent) {
        return false;
    }

    @Override
    public boolean handle(Object userData) {
        if (userData instanceof Bullet) {
            Bullet bullet = (Bullet) userData;
            return handle(bullet);
        }
        return false;
    }
    
    protected abstract boolean handle(Bullet bullet);
}
