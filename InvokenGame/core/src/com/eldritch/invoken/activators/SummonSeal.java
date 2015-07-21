package com.eldritch.invoken.activators;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.location.ConnectedRoom;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.location.NaturalVector2;
import com.google.common.base.Optional;

public class SummonSeal extends ProximityActivator {
    private static final float RADIUS = 1f;

    private final List<Agent> agents = new ArrayList<>();
    private Optional<ConnectedRoom> room = Optional.absent();
    private boolean released = false;

    public SummonSeal(NaturalVector2 position) {
        super(position, ProximityParams.of(new Vector2(position.x + RADIUS, position.y + RADIUS),
                Vector2.Zero, RADIUS));
    }

    @Override
    protected boolean onProximityChange(boolean hasProximity, Level level) {
        if (hasProximity && !released) {
            release();
        }
        return false;
    }

    @Override
    protected void postRegister(Level level) {
        room = level.getRoomFor(NaturalVector2.of(getCenter()));

        // select some subset of the residents to be in stasis under this seal
        if (room.isPresent()) {
            for (Agent resident : room.get().getResidents()) {
                add(resident);
            }
        }
    }

    private void add(Agent agent) {
        // stasis means both inactive (not rendered or affected by physics)
        // and paralyzed (unable to act)
        agent.setActive(false);
        agent.setParalyzed(true);
        agents.add(agent);
    }

    private void release() {
        for (Agent agent : agents) {
            agent.setActive(true);
            agent.setParalyzed(false);
        }
        released = true;
    }
}
