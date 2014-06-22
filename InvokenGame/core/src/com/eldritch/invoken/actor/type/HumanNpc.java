package com.eldritch.invoken.actor.type;

import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.proto.Actors.NonPlayerActor;

public class HumanNpc extends Npc {
    public HumanNpc(NonPlayerActor data, float x, float y, String asset, Location location) {
        super(data, x, y, Human.getWidth(), Human.getHeight(), Human.getAllAnimations(asset),
                location);
    }
    
    @Override
    public float getMaxVelocity() {
        return Human.MAX_VELOCITY;
    }
}
