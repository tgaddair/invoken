package com.eldritch.invoken.activators;

import com.eldritch.invoken.actor.type.Agent;

public interface ProximityActivator extends Activator {
    boolean inProximity(Agent agent);
}
