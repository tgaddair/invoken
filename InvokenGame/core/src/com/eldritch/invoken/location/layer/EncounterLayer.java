package com.eldritch.invoken.location.layer;

import com.eldritch.invoken.proto.Locations.Encounter;

public class EncounterLayer extends LocationLayer {
    public final Encounter encounter;

    public EncounterLayer(Encounter encounter, int width, int height, int tileWidth,
            int tileHeight, LocationMap map) {
        super(width, height, tileWidth, tileHeight, map);
        this.encounter = encounter;
    }
}