package com.eldritch.invoken.location.layer;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;

public class RemovableCell {
    private final Cell cell;
    private final LocationLayer layer;
    private final int x;
    private final int y;
    
    public RemovableCell(Cell cell, LocationLayer layer, int x, int y) {
        this.cell = cell;
        this.layer = layer;
        this.x = x;
        this.y = y;
    }
    
    public void set(boolean removed) {
        if (removed) {
            remove();
        } else {
            add();
        }
    }
    
    public void remove() {
        layer.setCell(x, y, null);
    }
    
    public void add() {
        layer.setCell(x, y, cell);
    }
}
