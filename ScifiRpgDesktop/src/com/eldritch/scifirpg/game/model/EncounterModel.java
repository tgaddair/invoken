package com.eldritch.scifirpg.game.model;

public class EncounterModel<T extends AbstractEncounter> {
    private final T encounter;
    
    public EncounterModel(T encounter) {
        this.encounter = encounter;
    }
    
    public T getEncounter() {
        return encounter;
    }
}
