package com.eldritch.invoken.encounter.layer;

import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.eldritch.invoken.encounter.NaturalVector2;

public class LocationCell extends Cell {
    private final NaturalVector2 position;
    private final LocationLayer layer;
    
    public LocationCell(NaturalVector2 position, LocationLayer layer) {
        this.position = position;
        this.layer = layer;
    }
    
    @Override
    public TiledMapTile getTile () {
        if (layer.isVisible(position.x, position.y)) {
            return super.getTile();
        }
        return null;
    }
}
