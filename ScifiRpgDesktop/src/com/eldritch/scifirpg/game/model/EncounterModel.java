package com.eldritch.scifirpg.game.model;

import com.eldritch.scifirpg.proto.Locations.Encounter.Type;

public class EncounterModel<T extends AbstractEncounter> {
    private final T encounter;
    private final LocationModel locationModel;
    private String nextLocation = null;
    private String successor = null;
    
    public EncounterModel(T encounter, LocationModel locationModel) {
        this.encounter = encounter;
        this.locationModel = locationModel;
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
