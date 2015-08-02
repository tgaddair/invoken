package com.eldritch.invoken.actor.ai.planning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.eldritch.invoken.actor.ai.btree.Pursue;
import com.eldritch.invoken.actor.ai.planning.Desire.AbstractDesire;
import com.eldritch.invoken.actor.type.BasicLocatable;
import com.eldritch.invoken.actor.type.Npc;
import com.eldritch.invoken.actor.util.Locatable;
import com.eldritch.invoken.location.ConnectedRoom;
import com.eldritch.invoken.location.NaturalVector2;

/**
 * Rallies agents to a choke point in a hallway.  Even better if it's on the critical path.
 */
public class Rally extends AbstractDesire {
    private static final float MIN_MDST = 10f;
    private static final float DURATION = 20f;

    private final List<Locatable> patrolPoints = new ArrayList<>();
    private int index = 0;

    private Locatable destination = null;
    private float elapsed = 0;

    public Rally(Npc owner) {
        super(owner);

        int i = 0;
        List<ConnectedRoom> rooms = new ArrayList<>(owner.getLocation().getRooms());
        Collections.shuffle(rooms);
        for (ConnectedRoom room : rooms) {
            NaturalVector2 center = room.getCenter();
            if (!owner.getLocation().isNavigable(center)) {
                // there must be a node in the graph for this room, otherwise we should skip it
                continue;
            }
            
            patrolPoints.add(new BasicLocatable(center));
            if (++i >= 3) {
                break;
            }
        }
        patrolPoints.add(new BasicLocatable(owner.getNaturalPosition()));
    }
    
    @Override
    protected void onStart() {
        // always choose a new destination to prevent thrashing
        setDestination();
    }

    @Override
    public void activeUpdate(float delta) {
        if (destination == null) {
            setDestination();
        } else {
            updateDestination(delta);
        }
    }

    @Override
    public float getValue() {
        if (owner.hasSquad() && owner.getSquad().getLeader() == owner) {
            return 1f;
        }
        return 0f;
    }

    private void updateDestination(float delta) {
        elapsed += delta;
        NaturalVector2 current = owner.getNaturalPosition();
        if (destination == current || current.mdst(destination.getNaturalPosition()) < MIN_MDST
                || elapsed > DURATION || !owner.getLastSeen().hasPath()) {
            setDestination();
        }
    }

    private void setDestination() {
        // navigate towards this agent as part of a routine patrol
        setDestination(patrolPoints.get(index));
        index = (index + 1) % patrolPoints.size();
    }

    private void setDestination(Locatable point) {
        this.destination = point;
        owner.locate(point);
        elapsed = 0;
    }

    @Override
    public boolean act() {
        if (destination == null) {
            setDestination();
        }
        
        owner.getLastSeen().setPosition(destination);
        Pursue.act(owner);
        return true;
    }
}
