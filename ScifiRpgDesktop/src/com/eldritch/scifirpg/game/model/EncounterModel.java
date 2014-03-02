package com.eldritch.scifirpg.game.model;

import java.util.ArrayList;
import java.util.List;

import com.eldritch.scifirpg.game.model.actor.Player;
import com.eldritch.scifirpg.game.util.OutcomeApplier;
import com.eldritch.scifirpg.proto.Locations.Encounter.Type;
import com.eldritch.scifirpg.proto.Outcomes.Outcome;

public class EncounterModel<T extends AbstractEncounter, S extends EncounterListener> {
    private final T encounter;
    private final OutcomeApplier applier;
    private final LocationModel locationModel;
    private final Player player;
    private final List<S> listeners = new ArrayList<>();
    private String nextLocation = null;
    private String successor = null;
    
    public EncounterModel(T encounter, GameState state) {
        this(encounter, state, new OutcomeApplier());
    }
    
    public EncounterModel(T encounter, GameState state, OutcomeApplier applier) {
        this.encounter = encounter;
        this.applier = applier;
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
        applyOutcomes(outcomes, applier);
    }
    
    public void applyOutcomes(List<Outcome> outcomes, OutcomeApplier oa) {
        List<Outcome> applied = oa.apply(outcomes, player, this);
        for (S listener : listeners) {
            listener.outcomesApplied(applied);
        }
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
    
    public void addListener(S listener) {
        listeners.add(listener);
    }
    
    protected final List<S> getListeners() {
        return listeners;
    }
}
