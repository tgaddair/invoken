package com.eldritch.invoken.location.proc;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.proto.Locations.Encounter;
import com.google.common.base.Optional;

public class EncounterSelector {
    private final int floor;
    private final List<Encounter> encounterList = new ArrayList<>();
    private final Set<Encounter> encounters;

    public EncounterSelector(int floor, List<Encounter> encounters) {
        this.floor = floor;
        this.encounterList.addAll(encounters);
        this.encounters = new LinkedHashSet<>(encounterList);
    }

    public Optional<Encounter> select(EncounterMatcher matcher) {
        com.eldritch.invoken.util.EncounterProvider selector = InvokenGame.ENCOUNTERS;

        // find all the available encounters for the given control point
        double total = 0;
        List<Encounter> available = new ArrayList<>();
        for (Encounter encounter : encounters) {
            if (matcher.matches(encounter)) {
                total += selector.getWeight(encounter, floor);
                available.add(encounter);
            }
        }

        // System.out.println("choosing for " + cp.getId());
        // System.out.println("available: " + available.size());

        // sample an encounter with replacement by its weight
        double target = Math.random() * total;
        double sum = 0;
        for (Encounter encounter : available) {
            sum += selector.getWeight(encounter, floor);
            if (sum >= target) {
                return choose(encounter);
            }
        }

        // no encounter found
        return Optional.absent();
    }
    
    private Optional<Encounter> choose(Encounter encounter) {
        if (encounter.getUnique()) {
            // remove the encounter from the list of possibilities
            encounters.remove(encounter);
        }
        
        return Optional.of(encounter);
    }

    public interface EncounterMatcher {
        boolean matches(Encounter encounter);
    }
}
