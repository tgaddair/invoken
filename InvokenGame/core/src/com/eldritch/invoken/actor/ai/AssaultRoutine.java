package com.eldritch.invoken.actor.ai;

import java.util.Collection;

import com.eldritch.invoken.actor.Agent;
import com.eldritch.invoken.actor.Npc;
import com.eldritch.invoken.encounter.Location;

public class AssaultRoutine extends AttackRoutine {
    public AssaultRoutine(Npc npc, Location location) {
        super(npc, location);
    }
    
    @Override
    public boolean isValid() {
        return npc.getBehavior().shouldAssault(location.getActors());
    }

    @Override
    protected void fillTargets(Collection<Agent> targets) {
        targets.clear();
        npc.getBehavior().getAssaultTargets(location.getActors(), targets);
    }
}
