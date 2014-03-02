package com.eldritch.scifirpg.game.model;

import java.util.List;

import com.eldritch.scifirpg.game.model.actor.Player;
import com.eldritch.scifirpg.proto.Locations.Encounter.Type;
import com.eldritch.scifirpg.proto.Outcomes.Outcome;

public class EncounterModel<T extends AbstractEncounter> {
    private final T encounter;
    private final LocationModel locationModel;
    private final Player player;
    private String nextLocation = null;
    private String successor = null;
    
    public EncounterModel(T encounter, GameState state) {
        this.encounter = encounter;
        this.locationModel = state.getLocationModel();
        this.player = state.getActorModel().getPlayer();
        if (encounter.hasSuccessor()) {
            successor = encounter.getSuccessorId();
        }
    }
    
    public void teleport(String locid) {
        nextLocation = locid;
    }
    
    public void nextEncounter() {
        // Don't trust the caller
        if (canContinue()) {
            if (nextLocation != null) {
                locationModel.setCurrent(nextLocation);
            } else if (successor != null) {
                locationModel.nextEncounter(successor);
            } else {
                locationModel.nextEncounter();
            }
        }
    }
    
    public void setSuccessor(String encounter) {
        successor = encounter;
    }
    
    public void applyOutcomes(List<Outcome> outcomes) {
        
    }
    
    public Type getType() {
        return encounter.getType();
    }
    
    public T getEncounter() {
        return encounter;
    }
    
    public boolean canContinue() {
        return true;
    }
}
