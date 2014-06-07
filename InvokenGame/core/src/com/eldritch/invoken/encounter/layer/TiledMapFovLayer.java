package com.eldritch.invoken.encounter.layer;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;

public class TiledMapFovLayer extends TiledMapTileLayer {
    public TiledMapFovLayer(int width, int height, int tileWidth, int tileHeight) {
        super(width, height, tileWidth, tileHeight);
    }
    
    @Override
    public Cell getCell(int x, int y) {
        if (!isVisible(x, y)) {
            return null;
        }
        return super.getCell(x, y);
    }
    
    private boolean isVisible(int x, int y) {
        return true;
    }
}
