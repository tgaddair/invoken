package com.eldritch.scifirpg.game.model;

import com.eldritch.invoken.proto.Actors.DialogueTree;
import com.eldritch.invoken.proto.Locations.Encounter;
import com.eldritch.invoken.proto.Locations.Encounter.DecisionParams;

public class DecisionEncounter extends AbstractEncounter {
    private final DecisionParams params;

    public DecisionEncounter(Encounter data) {
        super(data);
        this.params = data.getDecisionParams();
    }

    public DialogueTree getDecisionTree() {
        return params.getDecisionTree();
    }
    
    public DecisionEncounterModel createModel(GameState state) {
        return new DecisionEncounterModel(this, state);
    }
}
