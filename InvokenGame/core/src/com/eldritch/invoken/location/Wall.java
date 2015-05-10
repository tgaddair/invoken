package com.eldritch.invoken.location;

import com.eldritch.invoken.actor.AgentHandler;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.util.Settings;


public class Wall implements AgentHandler {
    private Wall() {}
    
    @Override
    public boolean handle(Agent agent) {
        return false;
    }

    @Override
    public boolean handle(Object userData) {
        return false;
    }
    
    private static class Holder {
        private static final Wall INSTANCE = new Wall();
    }

    public static Wall getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public short getCollisionMask() {
        return Settings.BIT_ANYTHING;
    }
}
