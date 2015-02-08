package com.eldritch.invoken.actor.ai.btree;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Npc;

public abstract class AbstractCombatTask extends LeafTask<Npc> {
    private final List<Agent> targets = new ArrayList<Agent>();
    
    protected final void fillTargets(Npc entity) {
        targets.clear();
        entity.getBehavior().getAssaultTargets(entity.getVisibleNeighbors(), targets);
    }
    
    protected final Agent selectBestTarget(Npc entity) {
        // get one of our enemies
        Agent current = null;
        float bestDistance = Float.MAX_VALUE;
        for (Agent agent : targets) {
            if (!agent.isAlive()) {
                // no point in attacking a dead enemy
                continue;
            }

            float distance = entity.dst2(agent);
            if (current == null || distance < bestDistance) {
                // attack the closer enemy
                current = agent;
                bestDistance = distance;
            }
        }
        return current;
    }
}
