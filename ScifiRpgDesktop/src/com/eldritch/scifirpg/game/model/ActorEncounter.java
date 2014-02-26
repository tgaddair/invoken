package com.eldritch.scifirpg.game.model;

import com.eldritch.scifirpg.proto.Locations.Encounter;
import com.eldritch.scifirpg.proto.Locations.Encounter.ActorParams;

public class ActorEncounter extends AbstractEncounter {
    private final ActorParams params;

    public ActorEncounter(Encounter data) {
        super(data);
        this.params = data.getActorParams();
    }
}
