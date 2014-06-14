package com.eldritch.invoken.actor;

import com.eldritch.invoken.encounter.Location;
import com.eldritch.scifirpg.proto.Actors.NonPlayerActor;

public class HumanNpc extends Npc {
    public HumanNpc(NonPlayerActor data, float x, float y, String asset, Location location) {
        super(data, x, y, Human.getWidth(), Human.getHeight(), Human.getAllAnimations(asset),
                location);
    }
}
