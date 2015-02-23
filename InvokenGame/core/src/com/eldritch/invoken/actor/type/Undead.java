package com.eldritch.invoken.actor.type;

import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.proto.Actors.NonPlayerActor;

public class Undead extends HumanNpc {
    public static float MAX_VELOCITY = 15f;
    
    public Undead(NonPlayerActor data, float x, float y, String asset, Location location) {
        super(data, x, y, MAX_VELOCITY, asset, location);
    }
}
