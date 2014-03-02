package com.eldritch.scifirpg.game.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.eldritch.scifirpg.game.util.LocationMarshaller;
import com.eldritch.scifirpg.proto.Locations.Encounter;
import com.eldritch.scifirpg.proto.Locations.Location;

public class LocationModel {
    private final GameState state;
    private final LocationMarshaller locationMarshaller = new LocationMarshaller();
    private final Map<String, AbstractEncounter> encounters = new LinkedHashMap<>();
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
    
    public AbstractEncounter getCurrentEncounter() {
        return encounter;
    }
    
    public final void setCurrent(String locid) {
        location = locationMarshaller.readAsset(locid);
        
        // Rebuild encounters
        List<AbstractEncounter> encList = new ArrayList<>();
        for (Encounter encounter : location.getEncounterList()) {
            encList.add(AbstractEncounter.getEncounter(encounter));
        }
        Collections.sort(encList);
        
        // Add the sorted encounters to our ordered map
        encounters.clear();
        for (AbstractEncounter encounter : encList) {
            encounters.put(encounter.getId(), encounter);
        }
        
        // Draw the next encounter
        nextEncounter();
    }
    
    public void nextEncounter() {
        nextEncounter(drawEncounter());
    }
    
    public void nextEncounter(String encounterId) {
        nextEncounter(encounters.get(encounterId));
    }
    
    private void nextEncounter(AbstractEncounter next) {
        encounter = next;
        
        // Notify listeners
        for (LocationListener listener : listeners) {
            listener.locationChanged(location);
        }
    }
    
    private AbstractEncounter drawEncounter() {
        double total = 0.0;
        List<AbstractEncounter> validEncounters = new ArrayList<>();
        for (AbstractEncounter encounter : encounters.values()) {
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
