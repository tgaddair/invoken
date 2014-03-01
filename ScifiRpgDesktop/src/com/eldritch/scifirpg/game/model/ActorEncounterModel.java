package com.eldritch.scifirpg.game.model;

import java.util.List;

import com.eldritch.scifirpg.game.model.ActorModel.Npc;

/**
 * Handles the internal state of a single ActorEncounter. Makes requests to the
 * global ActorModel for data and updates the ActorModel's persistent state
 * (who's alive, etc.).
 * 
 */
public class ActorEncounterModel extends EncounterModel<ActorEncounter> {
    private final ActorModel model;

    public ActorEncounterModel(ActorEncounter encounter, ActorModel model) {
        super(encounter);
        this.model = model;
    }
    
    public Player getPlayer() {
        return model.getPlayer();
    }
    
    public List<Npc> getActors() {
        return model.getActorsFor(getEncounter());
    }
    
    public ActorModel getActorModel() {
        return model;
    }
}
