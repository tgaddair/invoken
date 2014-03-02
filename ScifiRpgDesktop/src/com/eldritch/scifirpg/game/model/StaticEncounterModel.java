package com.eldritch.scifirpg.game.model;

public class StaticEncounterModel extends EncounterModel<StaticEncounter, EncounterListener> {
    public StaticEncounterModel(StaticEncounter encounter, GameState state) {
        super(encounter, state);
    }
    
    public void init() {
        // Once we have our listeners in place, go ahead and apply outcomes
        applyOutcomes(getEncounter().getOutcomes());
    }
}
