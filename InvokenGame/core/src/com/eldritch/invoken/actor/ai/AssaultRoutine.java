package com.eldritch.invoken.actor.ai;

import java.util.Collection;

import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Npc;
import com.eldritch.invoken.encounter.Location;

public class AssaultRoutine extends AttackRoutine {
    public AssaultRoutine(Npc npc, Location location) {
        super(npc, location);
    }
    
    @Override
    public boolean isValid() {
        return npc.getBehavior().shouldAssault(npc.getNeighbors());
    }

    @Override
    protected void fillTargets(Collection<Agent> targets) {
        targets.clear();
        npc.getBehavior().getAssaultTargets(npc.getNeighbors(), targets);
    }
}
