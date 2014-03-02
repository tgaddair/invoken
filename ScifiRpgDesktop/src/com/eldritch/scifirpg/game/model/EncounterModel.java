package com.eldritch.scifirpg.game.model;

import com.eldritch.scifirpg.proto.Locations.Encounter.Type;

public class EncounterModel<T extends AbstractEncounter> {
    private final T encounter;
    private final LocationModel locationModel;
    private String successor = null;
    
    public EncounterModel(T encounter, LocationModel locationModel) {
        this.encounter = encounter;
        this.locationModel = locationModel;
        if (encounter.hasSuccessor()) {
            successor = encounter.getSuccessorId();
        }
    }
    
    public void setSuccessor(String encounter) {
        successor = encounter;
    }
    
    public Type getType() {
        return encounter.getType();
    }
    
    public T getEncounter() {
        return encounter;
    }
    
    public void nextEncounter() {
        // Don't trust the caller
        if (canContinue()) {
            if (successor != null) {
                locationModel.nextEncounter(successor);
            } else {
                locationModel.nextEncounter();
            }
        }
    }
    
    public boolean canContinue() {
        return true;
    }
}
