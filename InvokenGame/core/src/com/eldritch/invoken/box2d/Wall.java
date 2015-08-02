package com.eldritch.invoken.box2d;

import com.eldritch.invoken.box2d.AgentHandler.DefaultAgentHandler;

public class Wall extends DefaultAgentHandler {
    private Wall() {}
    
    private static class Holder {
        private static final Wall INSTANCE = new Wall();
    }

    public static Wall getInstance() {
        return Holder.INSTANCE;
    }
}
