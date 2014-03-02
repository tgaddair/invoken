package com.eldritch.scifirpg.game.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.eldritch.scifirpg.game.model.actor.ActorEncounter;
import com.eldritch.scifirpg.game.model.actor.ActorEncounterModel;
import com.eldritch.scifirpg.game.util.LocationMarshaller;
import com.eldritch.scifirpg.game.view.ActorEncounterPanel;
import com.eldritch.scifirpg.game.view.RegionEncounterPanel;
import com.eldritch.scifirpg.game.view.StaticEncounterPanel;
import com.eldritch.scifirpg.proto.Locations.Encounter;
import com.eldritch.scifirpg.proto.Locations.Location;

public class LocationModel {
    private final GameState state;
    private final LocationMarshaller locationMarshaller = new LocationMarshaller();
    private final List<AbstractEncounter> encounters = new ArrayList<>();
    private final List<LocationListener> listeners = new ArrayList<>();
    private Location location;
    private AbstractEncounter encounter;

    public LocationModel(String locid, GameState state) {
        this.state = state;
        setCurrent(locid);
    }
    
    public void returnToPreviousLocation() {
        if (location.hasParentId()) {
            setCurrent(location.getParentId());
        }
    }
    
    public final void setCurrent(String locid) {
        location = locationMarshaller.readAsset(locid);
        
        // Rebuild encounters
        encounters.clear();
        for (Encounter encounter : location.getEncounterList()) {
            encounters.add(AbstractEncounter.getEncounter(encounter));
        }
        Collections.sort(encounters);
        
        // Draw an encounter
        drawNextEncounter();
        
        // Notify listeners
        for (LocationListener listener : listeners) {
            listener.locationChanged(location);
        }
    }
    
    public AbstractEncounter getCurrentEncounter() {
        return encounter;
    }
    
    public void nextEncounter() {
        drawNextEncounter();
        
        // Notify listeners
        for (LocationListener listener : listeners) {
            listener.locationChanged(location);
        }
    }
    
    private void drawNextEncounter() {
        encounter = drawEncounter();
    }
    
    private AbstractEncounter drawEncounter() {
        double total = 0.0;
        List<AbstractEncounter> validEncounters = new ArrayList<>();
        for (AbstractEncounter encounter : encounters) {
            if (encounter.satisfiesPrerequisites(state)) {
                if (encounter.getWeight() < 0.0) {
                    // The weight is negative, so we automatically draw it if
                    // it's the first encounter we find that satisfies its
                    // prerequisites
                    return encounter;
                } else {
                    validEncounters.add(encounter);
                    total += encounter.getWeight();
                }
            }
        }

        // No mandatory encounter satisfied prerequisites, so sample randomly
        double target = Math.random() * total;
        double sum = 0.0;
        for (AbstractEncounter encounter : validEncounters) {
            sum += encounter.getWeight();
            if (sum >= target) {
                return encounter;
            }
        }

        // This should not happen, but we should handle it elegantly if it does.
        // TODO: return to previous location?
        throw new IllegalStateException("No encounter in " + location.getName()
                + " satisfies its prerequisites");
    }
    
    public void register(LocationListener listener) {
        listeners.add(listener);
    }
    
    public interface LocationListener {
        void locationChanged(Location loc);
    }
}
