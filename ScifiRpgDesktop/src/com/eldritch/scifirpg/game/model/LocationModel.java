package com.eldritch.scifirpg.game.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.eldritch.scifirpg.game.util.LocationMarshaller;
import com.eldritch.scifirpg.proto.Locations.Encounter;
import com.eldritch.scifirpg.proto.Locations.Location;

public class LocationModel {
    private Location location;
    private final LocationMarshaller locationMarshaller = new LocationMarshaller();
    private final List<AbstractEncounter> encounters = new ArrayList<>();
    private final List<LocationListener> listeners = new ArrayList<>();

    public LocationModel(String locid) {
        location = locationMarshaller.readAsset(locid);
        for (Encounter encounter : location.getEncounterList()) {
            encounters.add(AbstractEncounter.getEncounter(encounter));
        }
        Collections.sort(encounters);
    }
    
    public void returnToPreviousLocation() {
        if (location.hasParentId()) {
            setCurrent(location.getParentId());
        }
    }
    
    public void setCurrent(String locid) {
        location = locationMarshaller.readAsset(locid);
    }

    public AbstractEncounter drawEncounter() {
        double total = 0.0;
        List<AbstractEncounter> validEncounters = new ArrayList<>();
        for (AbstractEncounter encounter : encounters) {
            if (encounter.satisfiesPrerequisites()) {
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
