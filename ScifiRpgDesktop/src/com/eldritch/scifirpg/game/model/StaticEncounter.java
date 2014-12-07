package com.eldritch.scifirpg.game.model;

import java.util.List;

import com.eldritch.invoken.proto.Locations.Encounter;
import com.eldritch.invoken.proto.Locations.Encounter.StaticParams;
import com.eldritch.invoken.proto.Outcomes.Outcome;

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
    
    public StaticEncounterModel createModel(GameState state) {
        return new StaticEncounterModel(this, state);
    }
}
