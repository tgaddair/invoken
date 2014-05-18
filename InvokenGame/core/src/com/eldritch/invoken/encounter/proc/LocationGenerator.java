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
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.Player;
import com.eldritch.invoken.encounter.Location;

public class LocationGenerator {
    private static final int PX = 32;
    private static final int SCALE = 2;
    private static final int MAX_LEAF_SIZE = 35;
    private final TextureAtlas atlas;
    private final TiledMapTile ground;

    public LocationGenerator() {
        atlas = new TextureAtlas(Gdx.files.internal("image-atlases/pages.atlas"));

        AtlasRegion region = atlas.findRegion("test-biome/floor4");
        ground = new StaticTiledMapTile(region);
    }

    public Location generate(Player player) {
        int width = 100;
        int height = 100;
        TiledMap map = getBaseMap(width, height);
        List<Leaf> leafs = createLeaves(width / SCALE, height / SCALE);

        // create layers
        TiledMapTileLayer base = createBaseLayer(leafs, width, height);
        map.getLayers().add(base);
        map.getLayers().add(createTrimLayer(base));
        map.getLayers().add(createOverlayLayer(base));
        map.getLayers().add(createSpawnLayer(width, height));

        com.eldritch.scifirpg.proto.Locations.Location proto = com.eldritch.scifirpg.proto.Locations.Location
                .getDefaultInstance();
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
        addCell(layer, tile, 50, 50);

        return layer;
    }

    private TiledMapTileLayer createBaseLayer(List<Leaf> leafs, int width, int height) {
        TiledMapTileLayer layer = new TiledMapTileLayer(width, height, PX, PX);
        layer.setVisible(true);
        layer.setOpacity(1.0f);
        layer.setName("base");

        for (Leaf leaf : leafs) {
            if (leaf.room != null) {
                addRoom(leaf.room, layer, ground);
            }
            for (Rectangle hall : leaf.halls) {
                addRoom(hall, layer, ground);
            }
        }

        // add walls
        addWalls(layer);

        return layer;
    }

    private TiledMapTileLayer createTrimLayer(TiledMapTileLayer base) {
        TiledMapTileLayer layer = new TiledMapTileLayer(base.getWidth(), base.getHeight(), PX, PX);
        layer.setVisible(true);
        layer.setOpacity(1.0f);
        layer.setName("trim");

        TiledMapTile leftTrim = new StaticTiledMapTile(atlas.findRegion("test-biome/left-trim"));
        TiledMapTile rightTrim = new StaticTiledMapTile(atlas.findRegion("test-biome/right-trim"));

        for (int x = 0; x < base.getWidth(); x++) {
            for (int y = 0; y < base.getHeight(); y++) {
                Cell cell = base.getCell(x, y);
                if (cell != null) {
                    if (x - 1 >= 0 && base.getCell(x - 1, y) == null) {
                        // empty space to the left
                        addCell(layer, leftTrim, x - 1, y);
                    }
                    if (x + 1 < base.getWidth() && base.getCell(x + 1, y) == null) {
                        // empty space to the right
                        addCell(layer, rightTrim, x + 1, y);
                    }
                }
            }
        }

        return layer;
    }
    
    private TiledMapTileLayer createOverlayLayer(TiledMapTileLayer base) {
        TiledMapTileLayer layer = new TiledMapTileLayer(base.getWidth(), base.getHeight(), PX, PX);
        layer.setVisible(true);
        layer.setOpacity(1.0f);
        layer.setName("overlay");

        TiledMapTile belowTrim = new StaticTiledMapTile(atlas.findRegion("test-biome/below-trim"));

        for (int x = 0; x < base.getWidth(); x++) {
            for (int y = 0; y < base.getHeight(); y++) {
                Cell cell = base.getCell(x, y);
                if (cell != null) {
                    if (y - 1 >= 0 && base.getCell(x, y - 1) == null) {
                        // empty space below
                        addCell(layer, belowTrim, x, y - 1);
                    }
                }
            }
        }

        return layer;
    }

    private void addWalls(TiledMapTileLayer layer) {
        TiledMapTile midWallTop = new StaticTiledMapTile(
                atlas.findRegion("test-biome/mid-wall-top"));
        TiledMapTile midWallCenter = new StaticTiledMapTile(
                atlas.findRegion("test-biome/mid-wall-center"));
        TiledMapTile midWallBottom = new StaticTiledMapTile(
                atlas.findRegion("test-biome/mid-wall-bottom"));

        for (int x = 0; x < layer.getWidth(); x++) {
            for (int y = 0; y < layer.getHeight(); y++) {
                Cell cell = layer.getCell(x, y);
                if (cell != null && cell.getTile() == ground) {
                    // check for empty space above
                    if (y + 3 < layer.getHeight() && layer.getCell(x, y + 3) == null) {
                        addCell(layer, midWallBottom, x, y + 1);
                        addCell(layer, midWallCenter, x, y + 2);
                        addCell(layer, midWallTop, x, y + 3);
                    }
                }
            }
        }
    }

    private void addRoom(Rectangle room, TiledMapTileLayer layer, TiledMapTile tile) {
        int left = (int) room.x;
        int right = (int) (room.x + room.width);
        int top = (int) room.y;
        int bottom = (int) (room.y + room.height);
        for (int i = left; i <= right; i++) {
            for (int j = top; j <= bottom; j++) {
                int startX = i * SCALE;
                int startY = j * SCALE;
                for (int x = 0; x < SCALE; x++) {
                    for (int y = 0; y < SCALE; y++) {
                        addCell(layer, tile, startX + x, startY + y);
                    }
                }
            }
        }
    }

    private void addCell(TiledMapTileLayer layer, TiledMapTile tile, int x, int y) {
        Cell cell = new Cell();
        cell.setTile(tile);
        layer.setCell(x, y, cell);
    }

    private List<Leaf> createLeaves(int width, int height) {
        List<Leaf> leafs = new ArrayList<Leaf>();

        // first, create a Leaf to be the 'root' of all Leafs.
        Leaf root = new Leaf(0, 0, width, height);
        leafs.add(root);

        boolean didSplit = true;
        // we loop through every Leaf in our Vector over and over again, until no more Leafs can be
        // split.
        while (didSplit) {
            didSplit = false;
            List<Leaf> newLeaves = new ArrayList<Leaf>();
            for (Leaf l : leafs) {
                // if this Leaf is not already split...
                if (l.leftChild == null && l.rightChild == null) {
                    // if this Leaf is too big, or 75% chance...
                    if (l.width > MAX_LEAF_SIZE || l.height > MAX_LEAF_SIZE || Math.random() > 0.25) {
                        if (l.split()) { // split the Leaf!
                            // if we did split, push the child leafs to the list so we can loop into
                            // them next
                            newLeaves.add(l.leftChild);
                            newLeaves.add(l.rightChild);
                            didSplit = true;
                        }
                    }
                }
            }
            leafs.addAll(newLeaves);
        }

        // next, iterate through each Leaf and create a room in each one.
        root.createRooms();

        return leafs;
    }

    public TextureAtlas getAtlas() {
        return atlas;
    }
}
