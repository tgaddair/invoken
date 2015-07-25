package com.eldritch.invoken.activators;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.GameCamera;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.location.ConnectedRoom;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.location.NaturalVector2;
import com.google.common.base.Optional;

public class SecurityCamera extends ClickActivator implements GameCamera {
    private static final int OFFSET = 2;

    private SecurityCamera next = null;
    private Optional<ConnectedRoom> room = Optional.absent();

    public SecurityCamera(NaturalVector2 position) {
        super(position.x + OFFSET, position.y + OFFSET, 1, 1, ProximityParams.of(
                new Vector2(position.x + OFFSET + 0.5f, position.y + 0.5f)).withIndicator(
                ProximityActivator.getIndicator(new Vector2(1f, 0.5f))));
    }

    @Override
    public void activate(Agent agent, Level level) {
        // if (next != null && agent.usingRemoteCamera()) {
        // agent.setCamera(next);
        // } else {
        // agent.resetCamera();
        // }

        if (next != null) {
            agent.setCamera(next);
        } else {
            agent.resetCamera();
        }

        if (room.isPresent()) {
            if (room.get().contains(agent.getCellPosition())) {
                System.out.println(agent.getInfo().getName() + " in room");
            } else {
                System.out.println(agent.getInfo().getName() + " outside room");
            }
        } else {
            System.out.println(agent.getInfo().getName() + " no room");
        }
    }

    @Override
    public void postRegister(Level level) {
        level.addSecurityCamera(this);
        room = level.getRoomFor(NaturalVector2.of(getCenter()));
    }

    public void setNext(SecurityCamera camera) {
        this.next = camera;
    }

    @Override
    protected boolean onProximityChange(boolean hasProximity, Level level) {
        return true;
    }

    @Override
    protected boolean canActivate(Agent agent) {
        return super.canActivate(agent) || agent.usingRemoteCamera();
    }
    
    @Override
    public float getZ() {
        return 0;
    }
}
