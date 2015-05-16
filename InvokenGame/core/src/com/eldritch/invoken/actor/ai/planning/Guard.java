package com.eldritch.invoken.actor.ai.planning;

import java.util.List;

import com.eldritch.invoken.actor.ai.btree.Pursue;
import com.eldritch.invoken.actor.ai.planning.Desire.AbstractDesire;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Npc;

public class Guard extends AbstractDesire {
    private static final float MIN_DST2 = 9f;
    private static final float DURATION = 20f;

    private Agent destination = null;
    private float elapsed = 0;
    
    public Guard(Npc owner) {
        super(owner);
    }

    @Override
    public void activeUpdate(float delta) {
        if (isLeader()) {
            planForLeader(delta);
        } else {
//            planForGuard(delta);
        }
    }

    @Override
    public float getValue() {
        return 0.5f;
    }

    private void planForGuard(float delta) {
        if (destination == null) {
            setDestination(owner.getSquad().getLeader());
        }
    }

    private void planForLeader(float delta) {
        if (destination == null) {
            setDestination(delta);
        } else {
            if (destination != null) {
                updateDestination(delta);
            }
        }
    }

    private void updateDestination(float delta) {
        elapsed += delta;
        if (destination == owner || owner.dst2(destination) < MIN_DST2 || elapsed > DURATION) {
            setDestination(delta);
        }
    }

    private void setDestination(float delta) {
        List<Agent> agents = owner.getLocation().getAllAgents();
        Agent agent = agents.get((int) (Math.random() * agents.size()));

        // navigate towards this agent as part of a routine patrol
        setDestination(agent);
    }

    private void setDestination(Agent destination) {
        this.destination = destination;
        owner.locate(destination);
        elapsed = 0;
    }

    private boolean isLeader() {
        if (!owner.hasSquad()) {
            return true;
        }
        return owner.getSquad().getLeader() == owner;
    }

    @Override
    public boolean act() {
        if (destination != null) {
            owner.setTarget(destination);
            Pursue.act(owner);
            return true;
        }
        return false;
    }
}
