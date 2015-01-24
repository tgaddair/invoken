package com.eldritch.invoken.encounter.layer;

import java.util.List;

import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.eldritch.invoken.encounter.NaturalVector2;
import com.eldritch.invoken.encounter.proc.FurnitureGenerator;

public class LocationLayer extends TiledMapTileLayer {
    private final LocationMap map;
    
    public LocationLayer(int width, int height, int tileWidth, int tileHeight, LocationMap map) {
        super(width, height, tileWidth, tileHeight);
        this.map = map;
    }
    
    public Cell addCell(TiledMapTile tile, int x, int y) {
        Cell cell = new LocationCell(NaturalVector2.of(x, y), this);
        cell.setTile(tile);
        setCell(x, y, cell);
        return cell;
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
    
    public Cell setGround(int x, int y) {
        Cell cell = new LocationCell(NaturalVector2.of(x, y), this);
        cell.setTile(map.getGround());
        setCell(x, y, cell);
        return cell;
    }
    
    public boolean isGround(Cell cell) {
        return cell != null && cell.getTile() == map.getGround();
    }

    public boolean isWall(int x, int y) {
        return inBounds(x, y) && getCell(x, y) != null
                && getCell(x, y).getTile() != map.getGround();
    }
    
    public boolean isFilled(int x, int y) {
    	return inBounds(x, y) && getCell(x, y) != null;
    }
    
    public static class CollisionLayer extends LocationLayer {
        private final TiledMapTile collider;
        
        public CollisionLayer(int width, int height, int tileWidth, int tileHeight, LocationMap map, TiledMapTile collider) {
            super(width, height, tileWidth, tileHeight, map);
            this.collider = collider;
        }
        
        public void addCell(int x, int y, List<RemovableCell> cells, FurnitureGenerator generator) {
            generator.addCell(this, collider, x, y, cells);
        }

        public void addCellIfAbsent(int x, int y,
                List<RemovableCell> cells, FurnitureGenerator generator) {
            if (getCell(x, y) == null) {
                generator.addCell(this, collider, x, y, cells);
            }
        }
    }
}
