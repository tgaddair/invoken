package com.eldritch.scifirpg.game.model;

import com.eldritch.scifirpg.proto.Locations.Encounter;
import com.eldritch.scifirpg.proto.Locations.Encounter.DecisionParams;

public class DecisionEncounter extends AbstractEncounter {
    private final DecisionParams params;

    public DecisionEncounter(Encounter data) {
        super(data);
        this.params = data.getDecisionParams();
    }
}
