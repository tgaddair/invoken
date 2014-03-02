package com.eldritch.scifirpg.game.model.actor;

import java.util.List;

import com.eldritch.scifirpg.game.model.AbstractEncounter;
import com.eldritch.scifirpg.proto.Locations.Encounter;
import com.eldritch.scifirpg.proto.Locations.Encounter.ActorParams;
import com.eldritch.scifirpg.proto.Locations.Encounter.ActorParams.ActorScenario;
import com.eldritch.scifirpg.proto.Outcomes.Outcome;

public class ActorEncounter extends AbstractEncounter {
    private final ActorParams params;

    public ActorEncounter(Encounter data) {
        super(data);
        this.params = data.getActorParams();
    }
    
    public String getDescription() {
        return params.getDescription();
    }
    
    public List<Outcome> getOnFlee() {
        return params.getOnFleeList();
    }
    
    public boolean canDetect() {
        return !params.getNoDetect();
    }
    
    public boolean canFlee() {
        return !params.getNoFlee();
    }
    
    public List<ActorScenario> getScenarios() {
        return params.getActorScenarioList();
    }
}
