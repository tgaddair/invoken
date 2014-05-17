package com.eldritch.invoken.encounter.proc;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Rectangle;
import com.eldritch.invoken.actor.Player;
import com.eldritch.invoken.encounter.Location;

public class LocationGenerator {
    private static final int PX = 32;
    private static final int MAX_LEAF_SIZE = 20;
    private final TextureAtlas atlas;
    
    public LocationGenerator() {
        atlas = new TextureAtlas(Gdx.files.internal("image-atlases/pages.atlas"));
    }

    public Location generate(Player player) {
        int width = 50;
        int height = 50;
        TiledMap map = getBaseMap(width, height);
        List<Leaf> leafs = createLeaves(width, height);

        // create layers
        map.getLayers().add(createBaseLayer(leafs, width, height));
        map.getLayers().add(createSpawnLayer(width, height));

        com.eldritch.scifirpg.proto.Locations.Location proto = 
                com.eldritch.scifirpg.proto.Locations.Location.getDefaultInstance();
        return new Location(proto, player, map);
    }

    private TiledMap getBaseMap(int width, int height) {
        TiledMap map = new TiledMap();
        MapProperties mapProperties = map.getProperties();
        mapProperties.put("width", width);
        mapProperties.put("height", height);
        mapProperties.put("tilewidth", PX);
        mapProperties.put("tileheight", PX);
        return map;
    }
    
    private TiledMapTileLayer createSpawnLayer(int width, int height) {
        TiledMapTileLayer layer = new TiledMapTileLayer(width, height, PX, PX);
        layer.setVisible(false);
        layer.setOpacity(1.0f);
        layer.setName("player");
        
        AtlasRegion region = getAtlas().findRegion("test-biome/floor1");
        TiledMapTile tile = new StaticTiledMapTile(region);
        Cell cell = new Cell();
        cell.setTile(tile);
        layer.setCell(10, 10, cell);
        
        return layer;
    }

    private TiledMapTileLayer createBaseLayer(List<Leaf> leafs, int width, int height) {
        TiledMapTileLayer layer = new TiledMapTileLayer(width, height, PX, PX);
        layer.setVisible(true);
        layer.setOpacity(1.0f);
        layer.setName("base");

        AtlasRegion region = getAtlas().findRegion("test-biome/floor1");
        TiledMapTile tile = new StaticTiledMapTile(region);

        for (Leaf leaf : leafs) {
            Rectangle room = leaf.room;
            int left = (int) room.x;
            int right = (int) (room.x + room.width);
            int top = (int) room.y;
            int bottom = (int) (room.y + room.height);
            for (int i = left; i <= right; i++) {
                for (int j = top; j <= bottom; j++) {
                    Cell cell = new Cell();
                    cell.setTile(tile);
                    layer.setCell(i, j, cell);
                }
            }
        }

        return layer;
    }

    private List<Leaf> createLeaves(int width, int height) {
        List<Leaf> leafs = new ArrayList<Leaf>();

        // first, create a Leaf to be the 'root' of all Leafs.
        Leaf root = new Leaf(0, 0, width, height);
        leafs.add(root);

        boolean did_split = true;
        // we loop through every Leaf in our Vector over and over again, until
        // no more Leafs can be split.
        while (did_split) {
            did_split = false;
            for (Leaf l : leafs) {
                // if this Leaf is not already split...
                if (l.leftChild == null && l.rightChild == null) {
                    // if this Leaf is too big, or 75% chance...
                    if (l.width > MAX_LEAF_SIZE || l.height > MAX_LEAF_SIZE || Math.random() > 0.25) {
                        if (l.split()) { // split the Leaf!
                            // if we did split, push the child leafs to the
                            // Vector so we can loop into them next
                            leafs.add(l.leftChild);
                            leafs.add(l.rightChild);
                            did_split = true;
                        }
                    }
                }
            }
        }

        // next, iterate through each Leaf and create a room in each one.
        root.createRooms();

        return leafs;
    }

    public TextureAtlas getAtlas() {
        return atlas;
    }
}
