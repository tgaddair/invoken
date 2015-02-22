package com.eldritch.invoken.encounter.proc;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.eldritch.invoken.encounter.layer.LocationLayer;
import com.eldritch.invoken.encounter.layer.LocationMap;
import com.eldritch.invoken.util.Settings;

public class IcarianFurnitureGenerator extends FurnitureGenerator {
    // tiles for different furniture pieces
    private final List<MultiTileStatic> servers = new ArrayList<MultiTileStatic>();
    private final MultiTileStatic table;
    private final MultiTileStatic cover;
    
    private int lastServerX = 0;

    public IcarianFurnitureGenerator(TextureAtlas atlas, TiledMapTile ground, long seed) {
        super(atlas, ground, seed);
        servers.add(new MultiTileStatic(atlas.findRegion("test-biome/furn-server1")));
        servers.add(new MultiTileStatic(atlas.findRegion("test-biome/furn-server2")));
        servers.add(new MultiTileStatic(atlas.findRegion("test-biome/furn-server3")));
        table = new MultiTileStatic(atlas.findRegion("test-biome/furn-table1"));
        cover = new MultiTileStatic(atlas.findRegion("test-biome/furn-core1"));
    }

    @Override
    public LocationLayer generateClutter(LocationLayer base, LocationMap map) {
        LocationLayer layer = new LocationLayer(base.getWidth(), base.getHeight(),
                (int) base.getTileWidth(), (int) base.getTileHeight(), map);
        layer.setVisible(true);
        layer.setOpacity(1.0f);
        layer.setName("clutter");

        // add clutter types
        placeServers(base, layer);
//        placeTables(base, layer);
//        placeCover(base, layer);
        
        return layer;
    }
    
    private void placeServers(LocationLayer base, LocationLayer layer) {
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
    }
    
    private void placeTables(LocationLayer base, LocationLayer layer) {
        for (int x = 0; x < base.getWidth(); x++) {
            for (int y = 0; y < base.getHeight(); y++) {
                Cell cell = base.getCell(x, y);
                if (base.isGround(cell) && Math.random() < 0.1) {
                    MultiTileStatic table = makeTable();
                    if (table.canPlaceAt(x,  y)) {
                        table.addTo(layer, x, y);
                    }
                }
            }
        }
    }
    
    private void placeCover(LocationLayer base, LocationLayer layer) {
        for (int x = 0; x < base.getWidth(); x++) {
            for (int y = 0; y < base.getHeight(); y++) {
                Cell cell = base.getCell(x, y);
                if (base.isGround(cell) && Math.random() < 0.1) {
                    MultiTileStatic cover = makeCover();
                    if (cover.canPlaceAt(x,  y)) {
                        cover.addTo(layer, x, y);
                    }
                }
            }
        }
    }
    
    private boolean shouldPlaceServer(MultiTileStatic server, int x, int y) {
        if (!server.canPlaceAt(x, y)) {
            return false;
        }
        
        boolean result = false;
        if (x == lastServerX + 1) {
            // high probability of placing another
            result = Math.random() < 0.7;
        } else {
            result = Math.random() < 0.1;
        }
        
        if (result) {
            lastServerX = x;
        }
        return result;
    }
    
    private MultiTileStatic makeCover() {
        return cover;
    }
    
    private MultiTileStatic makeTable() {
        return table;
    }
    
    private MultiTileStatic getServer() {
        int index = (int) (Math.random() * servers.size());
        return servers.get(index);
    }

    public class MultiTileStatic {
        private final TiledMapTile[][] tiles;

        public MultiTileStatic(TextureRegion texture) {
            TextureRegion[][] grid = texture.split(Settings.PX, Settings.PX);
            tiles = new TiledMapTile[grid.length][];
            for (int i = 0; i < grid.length; i++) {
                tiles[i] = new TiledMapTile[grid[i].length];
                for (int j = 0; j < grid[i].length; j++) {
                    StaticTiledMapTile tile = new StaticTiledMapTile(grid[i][j]);
                    tile.setOffsetY(-Settings.PX / 2);
                    tiles[i][j] = tile;
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
                    if (marked(x + j, y - i)) {
                        return false;
                    }
                }
            }
            return true;
        }
    }
}
