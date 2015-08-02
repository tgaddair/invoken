package com.eldritch.invoken.actor.util;

import com.eldritch.invoken.actor.type.Agent;

public interface SelectionHandler {
    boolean canSelect(Agent other);
    
    boolean select(Agent other);
}
