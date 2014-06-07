package com.eldritch.invoken.encounter.layer;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.eldritch.invoken.encounter.LocationMap;

public class LocationLayer extends TiledMapTileLayer {
    private final LocationMap map;
    
    public LocationLayer(int width, int height, int tileWidth, int tileHeight, LocationMap map) {
        super(width, height, tileWidth, tileHeight);
        this.map = map;
    }
    
    @Override
    public Cell getCell(int x, int y) {
        if (!isVisible(x, y)) {
            return null;
        }
        return super.getCell(x, y);
    }
    
    private boolean isVisible(int x, int y) {
        return map.isActive(x, y);
    }
}
