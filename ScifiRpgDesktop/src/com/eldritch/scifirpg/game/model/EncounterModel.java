package com.eldritch.scifirpg.game.model;

import com.eldritch.scifirpg.proto.Locations.Encounter.Type;

public class EncounterModel<T extends AbstractEncounter> {
    private final T encounter;
    private final LocationModel locationModel;
    
    public EncounterModel(T encounter, LocationModel locationModel) {
        this.encounter = encounter;
        this.locationModel = locationModel;
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
            locationModel.nextEncounter();
        }
    }
    
    public boolean canContinue() {
        return true;
    }
}
