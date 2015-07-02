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

public class Patrol extends AbstractDesire {
    private static final float MIN_MDST = 10f;
    private static final float DURATION = 20f;

    private final List<Locatable> patrolPoints = new ArrayList<>();
    private int index = 0;

    private Locatable destination = null;
    private float elapsed = 0;

    public Patrol(Npc owner) {
        super(owner);

        int i = 0;
        List<ConnectedRoom> rooms = new ArrayList<>(owner.getLocation().getRooms());
        Collections.shuffle(rooms);
        for (ConnectedRoom room : rooms) {
            patrolPoints.add(new BasicLocatable(room.getCenter()));
            if (++i >= 3) {
                break;
            }
        }
        patrolPoints.add(new BasicLocatable(owner.getNaturalPosition()));
    }

    @Override
    public void activeUpdate(float delta) {
        if (destination == null) {
            setDestination(delta);
        } else {
            if (destination != null) {
                updateDestination(delta);
            }
        }
    }

    @Override
    public float getValue() {
        return 0.5f;
    }

    private void updateDestination(float delta) {
        elapsed += delta;
        NaturalVector2 current = owner.getNaturalPosition();
        if (destination == current || current.mdst(destination.getNaturalPosition()) < MIN_MDST
                || elapsed > DURATION) {
            setDestination(delta);
        }
    }

    private void setDestination(float delta) {
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
        if (destination != null) {
            owner.getLastSeen().setPosition(destination);
            Pursue.act(owner);
            return true;
        }
        return false;
    }
}
