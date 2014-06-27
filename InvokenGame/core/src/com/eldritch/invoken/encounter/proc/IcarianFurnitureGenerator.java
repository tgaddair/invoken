package com.eldritch.invoken.encounter.proc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.encounter.NaturalVector2;
import com.eldritch.invoken.encounter.layer.LocationLayer;
import com.eldritch.invoken.encounter.layer.LocationMap;

public class IcarianFurnitureGenerator extends FurnitureGenerator {
    // tiles for different furniture pieces
    private final List<MultiTileStatic> servers = new ArrayList<MultiTileStatic>();

    public IcarianFurnitureGenerator(TextureAtlas atlas) {
        super(atlas);
        servers.add(new MultiTileStatic(atlas.findRegion("test-biome/furn-server1")));
        servers.add(new MultiTileStatic(atlas.findRegion("test-biome/furn-server2")));
        servers.add(new MultiTileStatic(atlas.findRegion("test-biome/furn-server3")));
    }

    @Override
    public LocationLayer generateClutter(LocationLayer base, TiledMapTile ground, LocationMap map) {
        LocationLayer layer = new LocationLayer(base.getWidth(), base.getHeight(),
                (int) base.getTileWidth(), (int) base.getTileHeight(), map);
        layer.setVisible(true);
        layer.setOpacity(1.0f);
        layer.setName("clutter");

        for (int x = 0; x < base.getWidth(); x++) {
            for (int y = 0; y < base.getHeight(); y++) {
                Cell c1 = base.getCell(x, y);
                Cell c2 = base.getCell(x, y - 1);
                if (c1 != null && c1.getTile() != ground && c2 != null && c2.getTile() == ground) {
                    MultiTileStatic server = getServer();
                    if (shouldPlaceServer(server, x, y + 1)) {
                        getServer().addTo(layer, x, y + 1);
                    }
                }
            }
        }
        
        return layer;
    }
    
    private int lastX = 0;
    
    private boolean shouldPlaceServer(MultiTileStatic server, int x, int y) {
        if (!server.canPlaceAt(x, y)) {
            return false;
        }
        
        boolean result = false;
        if (x == lastX + 1) {
            // high probability of placing another
            result = Math.random() < 0.75;
        } else {
            result = Math.random() < 0.2;
        }
        
        if (result) {
            lastX = x;
        }
        return result;
    }
    
    private MultiTileStatic getServer() {
        int index = (int) (Math.random() * servers.size());
        return servers.get(index);
    }

    public class MultiTileStatic {
        private final TiledMapTile[][] tiles;

        public MultiTileStatic(TextureRegion texture) {
            TextureRegion[][] grid = texture.split(Location.PX, Location.PX);
            tiles = new TiledMapTile[grid.length][];
            for (int i = 0; i < grid.length; i++) {
                tiles[i] = new TiledMapTile[grid[i].length];
                for (int j = 0; j < grid[i].length; j++) {
                    tiles[i][j] = new StaticTiledMapTile(grid[i][j]);
                }
            }
        }

        public void addTo(LocationLayer layer, int x, int y) {
            for (int i = 0; i < tiles.length; i++) {
                for (int j = 0; j < tiles[i].length; j++) {
                    addCell(layer, tiles[i][j], x + j, y - i);
                }
            }
        }
        
        public boolean canPlaceAt(int x, int y) {
            for (int i = 0; i < tiles.length; i++) {
                for (int j = 0; j < tiles[i].length; j++) {
                    if (isMarked(x + j, y - i)) {
                        return false;
                    }
                }
            }
            return true;
        }
    }
}
