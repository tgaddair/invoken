package com.eldritch.invoken.actor.ai;

import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Npc;

public class Planner {
    private final Npc owner;
    
    private boolean hasGoal = false;
    private Agent destination = null;
    
    public Planner(Npc owner) {
        this.owner = owner;
    }
    
    public Agent getDestination() {
        return destination;
    }
    
    public boolean hasGoal() {
        return hasGoal;
    }
    
    public void update(float delta) {
        if (owner.isGuard() && isLeader() && !hasGoal) {
            planForGuard(delta);
        }
    }
    
    private void planForGuard(float delta) {
        for (Agent agent : owner.getLocation().getAllAgents()) {
            if (owner != agent && !inSquad(agent) && owner.isAlly(agent)) {
                // navigate towards this agent as part of a routine patrol
                destination = agent;
                hasGoal = true;
            }
        }
    }
    
    private boolean isLeader() {
        if (!owner.hasSquad()) {
            return true;
        }
        return owner.getSquad().getLeader() == owner;
    }
    
    private boolean inSquad(Agent other) {
        if (!owner.hasSquad()) {
            return true;
        }
        return owner.getSquad().contains(other);
    }
}
