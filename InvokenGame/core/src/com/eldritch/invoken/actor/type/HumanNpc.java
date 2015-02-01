package com.eldritch.invoken.actor.type;

import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.proto.Actors.NonPlayerActor;

public class HumanNpc extends Npc {
    public HumanNpc(NonPlayerActor data, float x, float y, String asset, Location location) {
        this(data, x, y, Human.MAX_VELOCITY, asset, location);
    }

    public HumanNpc(NonPlayerActor data, float x, float y, float maxVelocity, String asset,
            Location location) {
        super(data, x, y, Human.getWidth(), Human.getHeight(), maxVelocity, Human
                .getAllAnimations(asset), location);
    }
}
