package com.eldritch.invoken.actor.pathfinding;

import com.eldritch.invoken.actor.type.Npc;

public abstract class AbstractSteeringBehavior implements SteeringBehavior {
    private final Npc npc;
    private boolean enabled = true;
    
    public AbstractSteeringBehavior(Npc npc) {
        this.npc = npc;
    }
    
    public Npc getNpc() {
        return npc;
    }
    
    @Override
    public double getPriority() {
        // default
        return 0;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
