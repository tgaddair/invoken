package com.eldritch.scifirpg.game.model;

import java.util.List;

import com.eldritch.scifirpg.proto.Locations.Encounter;
import com.eldritch.scifirpg.proto.Locations.Encounter.StaticParams;
import com.eldritch.scifirpg.proto.Outcomes.Outcome;

public class StaticEncounter extends AbstractEncounter {
    private final StaticParams params;

    public StaticEncounter(Encounter data) {
        super(data);
        this.params = data.getStaticParams();
    }
    
    public String getDescription() {
        return params.getDescription();
    }
    
    public List<Outcome> getOutcomes() {
        return params.getOutcomeList();
    }
    
    public boolean canRest() {
        return params.getRest();
    }
}
