package com.eldritch.invoken.activators;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.GameCamera;
import com.eldritch.invoken.actor.factions.Faction;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.location.ConnectedRoom;
import com.eldritch.invoken.location.CrimeManager.Crime;
import com.eldritch.invoken.location.CrimeManager.CrimeHandler;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.location.NaturalVector2;
import com.eldritch.invoken.util.Constants;
import com.google.common.base.Optional;

public class SecurityCamera extends ClickActivator implements GameCamera, CrimeHandler {
    private static final int OFFSET = 2;
    private static final float RELATION_DELTA = 100f;

    private SecurityCamera next = null;
    private Optional<ConnectedRoom> room = Optional.absent();
    private boolean active = true;

    public SecurityCamera(NaturalVector2 position) {
        super(position.x + OFFSET, position.y + OFFSET, 1, 1, ProximityParams.of(
                new Vector2(position.x + OFFSET + 0.5f, position.y + 0.5f)).withIndicator(
                ProximityActivator.getIndicator(new Vector2(1f, 0.5f))));
    }

    @Override
    public void activate(Agent agent, Level level) {
        if (next != null && agent.usingRemoteCamera()) {
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
        level.getCrimeManager().addHandler(this);
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

    @Override
    public void handle(Crime crime) {
        if (active && room.isPresent()) {
            Faction faction = crime.getPerpetrator().getLocation()
                    .getFaction(Constants.STATION_FACTION);
            if (crime.hasRoom() && crime.getRoom() == room.get() && crime.isOffenseAgainst(faction)) {
                // the camera is active and the crime took place in our room, so we can report it
                crime.report(faction);

                // now we need to lock down this room and call in the guards
                room.get().setLocked(true);
                for (Agent member : faction.getMembers()) {
                    System.out.println("alerting " + member.getInfo().getName() + " to "
                            + crime.getPerpetrator().getInfo().getName());
                    
                    member.changeRelation(crime.getPerpetrator(), -RELATION_DELTA);
                    member.alertTo(crime.getPerpetrator());
                }
                System.out.println("reporting crime!");
            }
        }
    }
}
