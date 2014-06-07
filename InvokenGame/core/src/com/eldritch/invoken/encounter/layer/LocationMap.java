package com.eldritch.invoken.encounter.layer;

import java.util.Set;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.eldritch.invoken.encounter.NaturalVector2;

public class LocationMap extends TiledMap {
    private Set<NaturalVector2> activeTiles = null;
    
    public void update(Set<NaturalVector2> activeTiles) {
        this.activeTiles = activeTiles;
    }
    
    public boolean isActive(int x, int y) {
        if (activeTiles == null) {
            // during initialization
            return true;
        }
        return activeTiles.contains(NaturalVector2.of(x, y));
    }
}
