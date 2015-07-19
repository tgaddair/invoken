package com.eldritch.invoken.location.layer;

import java.util.Set;

import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.eldritch.invoken.location.NaturalVector2;
import com.eldritch.invoken.util.Constants;
import com.eldritch.invoken.util.Settings;
import com.google.common.collect.Sets;

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
        private final Set<NaturalVector2> temporary = Sets.newHashSet();
        
        public CollisionLayer(int width, int height, int tileWidth, int tileHeight, LocationMap map) {
            super(width, height, tileWidth, tileHeight, map);
        }
        
        @Override
        public Cell addCell(TiledMapTile tile, int x, int y) {
            if (tile != null) {
                MapProperties props = tile.getProperties();
                if (props.containsKey(Constants.TRANSIENT) || props.containsKey(Constants.BLANK)) {
                    setTransient(x, y);
                }
            }
            return super.addCell(tile, x, y);
        }
        
        public void removeTransient() {
            for (NaturalVector2 point : temporary) {
                setCell(point.x, point.y, null);
            }
        }
        
        public void setTransient(int x, int y) {
            temporary.add(NaturalVector2.of(x, y));
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
    
    public static class WallLayer extends LocationLayer {
        private final int z;
        
        public WallLayer(int width, int height, int tileWidth, int tileHeight, LocationMap map, int z) {
            super(width, height, tileWidth, tileHeight, map);
            this.z = z;
        }
        
        public int getZ() {
            return z;
        }
    }
}
