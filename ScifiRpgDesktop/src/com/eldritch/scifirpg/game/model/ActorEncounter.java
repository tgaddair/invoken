package com.eldritch.scifirpg.game.model;

import java.util.ArrayList;
import java.util.List;

import com.eldritch.scifirpg.proto.Actors.NonPlayerActor;
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
    
    public List<NonPlayerActor> getActors(ActorModel model) {
        List<NonPlayerActor> actors = new ArrayList<>();
        for (ActorScenario scenario : params.getActorScenarioList()) {
            actors.add(model.getActor(scenario.getActorId()));
        }
        return actors;
    }
}
