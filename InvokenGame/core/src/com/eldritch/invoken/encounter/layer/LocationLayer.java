package com.eldritch.invoken.encounter.layer;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;

public class LocationLayer extends TiledMapTileLayer {
    private final LocationMap map;
    
    public LocationLayer(int width, int height, int tileWidth, int tileHeight, LocationMap map) {
        super(width, height, tileWidth, tileHeight);
        this.map = map;
    }
    
    public boolean isVisible(int x, int y) {
        return map.isActive(x, y);
    }
}
