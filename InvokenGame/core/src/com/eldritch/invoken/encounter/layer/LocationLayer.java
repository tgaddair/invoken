package com.eldritch.invoken.encounter.layer;

import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.eldritch.invoken.encounter.NaturalVector2;
import com.eldritch.invoken.util.Settings;

public class LocationLayer extends TiledMapTileLayer {
    private final LocationMap map;
    private final int scale;

    public LocationLayer(int width, int height, int tileWidth, int tileHeight, LocationMap map) {
        super(width, height, tileWidth, tileHeight);
        this.map = map;
        scale = Settings.PX / tileWidth;
    }

    public Cell addCell(TiledMapTile tile, int x, int y) {
        Cell cell = new LocationCell(NaturalVector2.of(x, y), this);
        cell.setTile(tile);
        setCell(x, y, cell);
        return cell;
    }
    
    public int getScale() {
        return scale;
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
        public CollisionLayer(int width, int height, int tileWidth, int tileHeight, LocationMap map) {
            super(width, height, tileWidth, tileHeight, map);
        }

        public boolean ignoresBullets(int x, int y) {
            Cell cell = getCell(x, y);
            if (cell != null && cell.getTile() != null) {
                MapProperties props = cell.getTile().getProperties();
                if (props.containsKey("collision")) {
                    if (props.get("collision").equals("short")) {
                        // bullets do not collide with this obstacle type
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
