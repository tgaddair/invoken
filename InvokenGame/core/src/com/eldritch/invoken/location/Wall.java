package com.eldritch.invoken.location;

import com.eldritch.invoken.actor.AgentHandler.DefaultAgentHandler;


public class Wall extends DefaultAgentHandler {
    private Wall() {}
    
    private static class Holder {
        private static final Wall INSTANCE = new Wall();
    }

    public static Wall getInstance() {
        return Holder.INSTANCE;
    }
}
