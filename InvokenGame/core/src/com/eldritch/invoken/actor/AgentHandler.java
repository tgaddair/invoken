package com.eldritch.invoken.actor;

import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.util.Settings;

public interface AgentHandler {
    boolean handle(Agent agent);
    
    boolean handle(Object userData);
    
    boolean handleEnd(Agent agent);
    
    boolean handleEnd(Object userData);
    
    short getCollisionMask();
    
    public static class DefaultAgentHandler implements AgentHandler {
        @Override
        public boolean handle(Agent agent) {
            return false;
        }

        @Override
        public boolean handle(Object userData) {
            return false;
        }
        
        @Override
        public boolean handleEnd(Agent agent) {
            return false;
        }

        @Override
        public boolean handleEnd(Object userData) {
            return false;
        }

        @Override
        public short getCollisionMask() {
            return Settings.BIT_ANYTHING;
        }
    }
}
