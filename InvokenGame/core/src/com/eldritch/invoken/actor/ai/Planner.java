package com.eldritch.invoken.actor.ai;

import java.util.List;

import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Npc;

public class Planner {
    private static final float MIN_DST2 = 9f;
    private static final float DURATION = 20f;
    
    private final Npc owner;
    
    private float elapsed = 0;
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
        if (owner.isGuard() && isLeader()) {
            if (!hasGoal) {
                planForGuard(delta);
            } else {
                if (destination != null) {
                    updateDestination(delta);
                }
            }
        }
    }
    
    private void updateDestination(float delta) {
        elapsed += delta;
        if (destination == owner || owner.dst2(destination) < MIN_DST2 || elapsed > DURATION) {
            planForGuard(delta);
        }
    }
    
    private void planForGuard(float delta) {
        List<Agent> agents = owner.getLocation().getAllAgents();
        Agent agent = agents.get((int) (Math.random() * agents.size()));
        
        // navigate towards this agent as part of a routine patrol
        destination = agent;
        setPlan();
    }
    
    private void setPlan() {
        hasGoal = true;
        elapsed = 0;
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
