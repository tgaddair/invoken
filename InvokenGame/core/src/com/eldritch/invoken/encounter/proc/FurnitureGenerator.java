package com.eldritch.invoken.encounter.proc;

import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.eldritch.invoken.encounter.layer.LocationLayer;
import com.eldritch.invoken.encounter.layer.LocationMap;

public interface FurnitureGenerator {
    LocationLayer generateClutter(LocationLayer base, TiledMapTile ground, LocationMap map);
}
