package com.eldritch.invoken.encounter.layer;

import java.util.List;

import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.eldritch.invoken.encounter.proc.LocationGenerator;

public class LocationLayer extends TiledMapTileLayer {
    private final LocationMap map;
    
    public LocationLayer(int width, int height, int tileWidth, int tileHeight, LocationMap map) {
        super(width, height, tileWidth, tileHeight);
        this.map = map;
    }
    
    public boolean isVisible(int x, int y) {
        return map.isActive(x, y);
    }
    
    public boolean inBounds(int x, int y) {
        return x >= 0 && x < getWidth() && y >= 0 && y < getHeight();
    }

    public boolean hasCell(int x, int y) {
        return inBounds(x, y) && getCell(x, y) != null;
    }
    
    public boolean hasAdjacentCell(int x0, int y0) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) {
                    continue;
                }
                
                int x = x0 + dx;
                int y = y0 + dy;
                if (hasCell(x, y)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public boolean isGround(int x, int y) {
        return inBounds(x, y) && isGround(getCell(x, y));
    }
    
    public boolean isGround(Cell cell) {
        return cell != null && cell.getTile() == map.getGround();
    }

    public boolean isWall(int x, int y) {
        return inBounds(x, y) && getCell(x, y) != null
                && getCell(x, y).getTile() != map.getGround();
    }
    
    public static class CollisionLayer extends LocationLayer {
        private final TiledMapTile collider;
        
        public CollisionLayer(int width, int height, int tileWidth, int tileHeight, LocationMap map, TiledMapTile collider) {
            super(width, height, tileWidth, tileHeight, map);
            this.collider = collider;
        }
        
        public void addCell(int x, int y, List<RemovableCell> cells) {
            LocationGenerator.addCell(this, collider, x, y, cells);
        }

        public void addCellIfAbsent(int x, int y,
                List<RemovableCell> cells) {
            if (getCell(x, y) == null) {
                LocationGenerator.addCell(this, collider, x, y, cells);
            }
        }
    }
}
