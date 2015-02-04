package com.eldritch.invoken.encounter.proc;

import java.util.ArrayList;
import java.util.List;

import com.eldritch.invoken.proto.Locations.Encounter;

public class EncounterGenerator extends BspGenerator {
    private final List<Encounter> encounters = new ArrayList<Encounter>();
    
    public EncounterGenerator(int roomCount, List<Encounter> encounters) {
        super(roomCount);
        this.encounters.addAll(encounters);
    }
}
