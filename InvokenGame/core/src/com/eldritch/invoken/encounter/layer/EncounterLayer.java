package com.eldritch.invoken.encounter.layer;

import com.eldritch.scifirpg.proto.Locations.Encounter;

public class EncounterLayer extends TiledMapFovLayer {
    public final Encounter encounter;

    public EncounterLayer(Encounter encounter, int width, int height, int tileWidth,
            int tileHeight) {
        super(width, height, tileWidth, tileHeight);
        this.encounter = encounter;
    }
}