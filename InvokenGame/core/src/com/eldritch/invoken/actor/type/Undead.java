package com.eldritch.invoken.actor.type;

import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.proto.Actors.NonPlayerActor;

public class Undead extends HumanNpc {
    public static float MAX_VELOCITY = 2f;
    public static float MAX_VELOCITY_CLOSE = 12f;
    public static float CLOSE_THRESHOLD = 10f;
    
    public Undead(NonPlayerActor data, float x, float y, String asset, Location location) {
        super(data, x, y, asset, location);
    }
    
    @Override
    public float getMaxVelocity() {
        if (hasTarget() && dst2(getTarget()) < CLOSE_THRESHOLD) {
            return MAX_VELOCITY_CLOSE;
        }
        return MAX_VELOCITY;
    }
}
