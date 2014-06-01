package com.eldritch.invoken.encounter.proc;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
import com.eldritch.invoken.actor.Agent;
import com.eldritch.invoken.actor.Player;
import com.eldritch.invoken.encounter.Activator;
import com.eldritch.invoken.encounter.DoorActivator;
import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.encounter.RemovableCell;
import com.eldritch.invoken.gfx.Light;
import com.eldritch.invoken.gfx.Light.StaticLight;
import com.eldritch.scifirpg.proto.Locations.Encounter;
import com.eldritch.scifirpg.proto.Locations.Encounter.ActorParams.ActorScenario;

public class LocationGenerator {
    private static final int PX = 32;
    private static final int SCALE = 2;
    private static final int MAX_LEAF_SIZE = 35;
    private final Random rand = new Random();
    private final TextureAtlas atlas;
    private final TiledMapTile ground;
    private final TiledMapTile midWallCenter;
    private final TiledMapTile leftTrim;
    private final TiledMapTile rightTrim;
    private final TiledMapTile doorLeft;
    private final TiledMapTile doorRight;
    private final TiledMapTile doorOverLeft;
    private final TiledMapTile doorOverRight;
    private final TiledMapTile doorOverLeftTop;
    private final TiledMapTile doorOverRightTop;
    private final TiledMapTile unlockedDoor;
    private final TiledMapTile lockedDoor;
    private final TiledMapTile collider;

    public LocationGenerator() {
        atlas = new TextureAtlas(Gdx.files.internal("image-atlases/pages.atlas"));

        AtlasRegion region = atlas.findRegion("test-biome/floor4");
        ground = new StaticTiledMapTile(region);
        midWallCenter = new StaticTiledMapTile(atlas.findRegion("test-biome/mid-wall-center"));
        leftTrim = new StaticTiledMapTile(atlas.findRegion("test-biome/left-trim"));
        rightTrim = new StaticTiledMapTile(atlas.findRegion("test-biome/right-trim"));
        doorLeft = new StaticTiledMapTile(
                atlas.findRegion("test-biome/door-front-bottom-left"));
        doorRight = new StaticTiledMapTile(
                atlas.findRegion("test-biome/door-front-bottom-right"));
        doorOverLeft = new StaticTiledMapTile(
                atlas.findRegion("test-biome/door-over-left"));
        doorOverRight = new StaticTiledMapTile(
                atlas.findRegion("test-biome/door-over-right"));
        doorOverLeftTop = new StaticTiledMapTile(
                atlas.findRegion("test-biome/door-over-left-top"));
        doorOverRightTop = new StaticTiledMapTile(
                atlas.findRegion("test-biome/door-over-right-top"));
        unlockedDoor = new StaticTiledMapTile(atlas.findRegion("test-biome/door-activator"));
        lockedDoor = new StaticTiledMapTile(atlas.findRegion("test-biome/door-activator-locked"));
        collider = new StaticTiledMapTile(atlas.findRegion("markers/collision"));
    }

    public Location generate(com.eldritch.scifirpg.proto.Locations.Location proto, Player player) {
        int width = 100;
        int height = 100;
        TiledMap map = getBaseMap(width, height);
        List<Leaf> leafs = createLeaves(width / SCALE, height / SCALE);

        List<Light> lights = new ArrayList<Light>();

        // create layers
        TiledMapTileLayer base = createBaseLayer(leafs, width, height);
        map.getLayers().add(base);

        TiledMapTileLayer trim = createTrimLayer(base);
        addLights(trim, base, lights);
        map.getLayers().add(trim);

        TiledMapTileLayer overlay = createOverlayLayer(base);
        map.getLayers().add(overlay);
        TiledMapTileLayer overlayTrim = createOverlayTrimLayer(base, overlay);
        map.getLayers().add(overlayTrim);

        TiledMapTileLayer collision = createCollisionLayer(base);
        map.getLayers().add(collision);
        map.getLayers().add(createSpawnLayer(base, collision, width, height));
        
        List<Activator> activators = new ArrayList<Activator>();
        createDoors(base, trim, overlay, overlayTrim, collision, activators);

        Location location = new Location(proto, player, map);
        location.addLights(lights);

        List<Agent> entities = createEntities(base, leafs, proto.getEncounterList(), location);
        location.addEntities(entities);
        location.addActivators(activators);

        return location;
    }

    private List<Agent> createEntities(TiledMapTileLayer layer, List<Leaf> leafs,
            List<Encounter> encounters, Location location) {
        List<Agent> entities = new ArrayList<Agent>();

        double total = getTotalWeight(encounters);
        for (Leaf leaf : leafs) {
            if (leaf.room != null) {
                addEncounter(leaf, entities, encounters, location, layer, total);
            }
        }

        return entities;
    }

    private void addEncounter(Leaf leaf, List<Agent> entities, List<Encounter> encounters,
            Location location, TiledMapTileLayer layer, double total) {
        String asset = "sprite/characters/male-fair.png"; // TODO: add to proto
        double target = Math.random() * total;
        double sum = 0.0;
        for (Encounter encounter : encounters) {
            if (encounter.getType() == Encounter.Type.ACTOR) {
                sum += encounter.getWeight();
                if (sum >= target) {
                    for (ActorScenario scenario : encounter.getActorParams().getActorScenarioList()) {
                        Vector2 position = getPoint(leaf.room);
                        if (layer.getCell((int) position.x, (int) position.y) != null) {
                            entities.add(location.createTestNpc(position, scenario.getActorId(),
                                    asset));
                        }
                    }
                    return;
                }
            }
        }
    }

    private double getTotalWeight(List<Encounter> encounters) {
        double total = 0.0;
        for (Encounter encounter : encounters) {
            if (encounter.getType() == Encounter.Type.ACTOR) {
                total += encounter.getWeight();
            }
        }
        return total;
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

        // fill in sides
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

        TiledMapTile leftCorner = new StaticTiledMapTile(atlas.findRegion("test-biome/left-corner"));
        TiledMapTile rightCorner = new StaticTiledMapTile(
                atlas.findRegion("test-biome/right-corner"));

        // fill in corners
        for (int x = 0; x < base.getWidth(); x++) {
            for (int y = 0; y < base.getHeight(); y++) {
                Cell cell = base.getCell(x, y);
                if (cell != null) {
                    if (x - 1 >= 0 && y - 1 >= 0 && base.getCell(x - 1, y - 1) == null
                            && layer.getCell(x - 1, y - 1) == null) {
                        // empty space in left corner
                        addCell(layer, leftCorner, x - 1, y - 1);
                    }
                    if (x + 1 >= 0 && y - 1 >= 0 && base.getCell(x + 1, y - 1) == null
                            && layer.getCell(x + 1, y - 1) == null) {
                        // empty space in left corner
                        addCell(layer, rightCorner, x + 1, y - 1);
                    }
                }
            }
        }

        return layer;
    }

    private void addLights(TiledMapTileLayer layer, TiledMapTileLayer base, List<Light> lights) {
        TiledMapTile light = new StaticTiledMapTile(atlas.findRegion("test-biome/light1"));
        for (int y = 0; y < base.getHeight(); y++) {
            // scan by row so we can properly distribute lights
            int lastLight = 0;
            for (int x = 0; x < base.getWidth(); x++) {
                Cell cell = base.getCell(x, y);
                if (cell != null && cell.getTile() == midWallCenter) {
                    // with some probability, add a light to the wall
                    if (lastLight == 1 && Math.random() < 0.75) {
                        addCell(layer, light, x, y);
                        lights.add(new StaticLight(new Vector2(x + 0.5f, y + 0.5f)));
                    }
                    lastLight = (lastLight + 1) % 5;
                } else {
                    // distribute along consecutive walls, so reset when there's a gap
                    lastLight = 0;
                }
            }
        }
    }

    private TiledMapTileLayer createOverlayLayer(TiledMapTileLayer base) {
        TiledMapTileLayer layer = new TiledMapTileLayer(base.getWidth(), base.getHeight(), PX, PX);
        layer.setVisible(true);
        layer.setOpacity(1.0f);
        layer.setName("overlay");

        TiledMapTile belowTrim = new StaticTiledMapTile(
                atlas.findRegion("test-biome/overlay-below-trim"));
        for (int x = 0; x < base.getWidth(); x++) {
            for (int y = 0; y < base.getHeight(); y++) {
                Cell cell = base.getCell(x, y);
                if (cell != null) {
                    if (y - 1 >= 0 && base.getCell(x, y - 1) == null) {
                        // empty space below
                        addCell(base, ground, x, y - 1);
                        addCell(layer, belowTrim, x, y - 1);
                    }
                }
            }
        }

        return layer;
    }

    private TiledMapTileLayer createOverlayTrimLayer(TiledMapTileLayer base,
            TiledMapTileLayer overlay) {
        TiledMapTileLayer layer = new TiledMapTileLayer(base.getWidth(), base.getHeight(), PX, PX);
        layer.setVisible(true);
        layer.setOpacity(1.0f);
        layer.setName("overlay-trim");

        TiledMapTile overlayLeftTrim = new StaticTiledMapTile(
                atlas.findRegion("test-biome/overlay-left-trim"));
        TiledMapTile overlayRightTrim = new StaticTiledMapTile(
                atlas.findRegion("test-biome/overlay-right-trim"));

        // fill in sides
        for (int x = 0; x < overlay.getWidth(); x++) {
            for (int y = 0; y < overlay.getHeight(); y++) {
                Cell cell = overlay.getCell(x, y);
                if (cell != null) {
                    if (isGround(x - 1, y, base) && overlay.getCell(x - 1, y) == null) {
                        // left space is ground
                        addCell(layer, overlayRightTrim, x, y);
                    }
                    if (isGround(x + 1, y, base) && overlay.getCell(x + 1, y) == null) {
                        // right space is ground
                        addCell(layer, overlayLeftTrim, x, y);
                    }
                }
            }
        }
        
        return layer;
    }

    private TiledMapTileLayer createCollisionLayer(TiledMapTileLayer base) {
        TiledMapTileLayer layer = new TiledMapTileLayer(base.getWidth(), base.getHeight(), PX, PX);
        layer.setVisible(true);
        layer.setOpacity(1.0f);
        layer.setName("collision");

        for (int x = 0; x < base.getWidth(); x++) {
            for (int y = 0; y < base.getHeight(); y++) {
                Cell cell = base.getCell(x, y);
                if (cell == null || cell.getTile() != ground) {
                    // empty space
                    addCell(layer, collider, x, y);
                }
            }
        }

        return layer;
    }

    private TiledMapTileLayer createSpawnLayer(TiledMapTileLayer base, TiledMapTileLayer collision,
            int width, int height) {
        TiledMapTileLayer layer = new TiledMapTileLayer(width, height, PX, PX);
        layer.setVisible(false);
        layer.setOpacity(1.0f);
        layer.setName("player");

        AtlasRegion region = atlas.findRegion("test-biome/floor1");
        TiledMapTile tile = new StaticTiledMapTile(region);

        for (int x = 0; x < base.getWidth(); x++) {
            for (int y = 0; y < base.getHeight(); y++) {
                Cell cell = base.getCell(x, y);
                Cell collisionCell = collision.getCell(x, y);
                if (cell != null && cell.getTile() == ground && collisionCell == null) {
                    addCell(layer, tile, x + 1, y + 1);
                    break;
                }
            }
        }

        return layer;
    }

    private void addWalls(TiledMapTileLayer layer) {
        TiledMapTile midWallTop = new StaticTiledMapTile(
                atlas.findRegion("test-biome/mid-wall-top"));
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
    
    private void createDoors(
            TiledMapTileLayer base, TiledMapTileLayer trim, TiledMapTileLayer overlay,
            TiledMapTileLayer overlayTrim, TiledMapTileLayer collision,
            List<Activator> activators) {
        addDoors(base, trim, overlay, collision, activators);
        addTrimDoors(base, trim, overlayTrim, collision, activators);
    }
    
    private void addDoors(TiledMapTileLayer base, TiledMapTileLayer trim,
            TiledMapTileLayer overlay, TiledMapTileLayer collision, List<Activator> activators) {
        
        TiledMapTile doorTopLeft = new StaticTiledMapTile(
                atlas.findRegion("test-biome/door-front-top-left"));
        TiledMapTile doorTopRight = new StaticTiledMapTile(
                atlas.findRegion("test-biome/door-front-top-right"));
        
        // add front doors
        List<RemovableCell> cells = new ArrayList<RemovableCell>();
        for (int x = 0; x < base.getWidth(); x++) {
            for (int y = 0; y < base.getHeight(); y++) {
                Cell cell = base.getCell(x, y);
                if (cell != null && cell.getTile() == ground) {
                    // wall to the left, wall to the right
                    if (isWall(x - 4, y, base) && isWall(x + 1, y, base)) {
                        if (isGround(x - 4, y - 1, base) && isGround(x + 1, y - 1, base)) {
                            // room below
                            addCell(trim, doorLeft, x - 3, y, cells);
                            addCell(trim, doorRight, x - 2, y, cells);
                            addCell(trim, doorLeft, x - 1, y, cells);
                            addCell(trim, doorRight, x, y, cells);
                            
                            // add overlay
                            addCell(overlay, doorTopLeft, x - 3, y + 1, cells);
                            addCell(overlay, doorTopRight, x - 2, y + 1, cells);
                            addCell(overlay, doorTopLeft, x - 1, y + 1, cells);
                            addCell(overlay, doorTopRight, x, y + 1, cells);
                            
                            // add collision
                            addCell(collision, collider, x - 3, y, cells);
                            addCell(collision, collider, x - 2, y, cells);
                            addCell(collision, collider, x - 1, y, cells);
                            addCell(collision, collider, x, y, cells);
                            
                            // add activator
                            DoorActivator activator = new DoorActivator(x - 4, y + 1, cells,
                                    unlockedDoor, lockedDoor);
                            activators.add(activator);
                            trim.setCell(x - 4, y + 1, activator.getCell());
                            cells.clear();
                        }
                    }
                }
            }
        }
    }
    
    private void addTrimDoors(TiledMapTileLayer base, TiledMapTileLayer trim,
            TiledMapTileLayer overlayTrim, TiledMapTileLayer collision,
            List<Activator> activators) {
        
        List<RemovableCell> cells = new ArrayList<RemovableCell>();
        for (int x = 0; x < base.getWidth(); x++) {
            for (int y = 0; y < base.getHeight(); y++) {
                Cell cell = base.getCell(x, y);
                if (cell != null && cell.getTile() == ground) {
                    // wall up, wall down
                    if (hasCell(x, y - 2, overlayTrim) && isWall(x, y + 1, base)) {
                        if (isGround(x - 1, y - 2, base) && isGround(x - 1, y + 1, base)) {
                            // room left
                            addTrimDoor(trim, overlayTrim, collision, doorOverLeft, doorOverLeftTop,
                                    x, y, activators, cells);
                        } else if (isGround(x + 1, y - 2, base) && isGround(x + 1, y + 1, base)) {
                            // room right
                            addTrimDoor(trim, overlayTrim, collision,
                                    doorOverRight, doorOverRightTop, x, y, activators, cells);
                        }
                    }
                }
            }
        }
    }
    
    private void addTrimDoor(TiledMapTileLayer trim, TiledMapTileLayer overlayTrim,
            TiledMapTileLayer collision, TiledMapTile tile, TiledMapTile top, int x, int y,
            List<Activator> activators, List<RemovableCell> cells) {
        // add the doors
        addCell(overlayTrim, tile, x, y - 1, cells);
        addCell(overlayTrim, tile, x, y, cells);
        addCell(overlayTrim, tile, x, y + 1, cells);
        addCell(overlayTrim, tile, x, y + 2, cells);
        addCell(overlayTrim, top, x, y + 3, cells);
        
        // add collision if absent so we don't delete collision cells when the door comes down
        addCellIfAbsent(collision, collider, x, y - 2, cells);
        addCellIfAbsent(collision, collider, x, y - 1, cells);
        addCellIfAbsent(collision, collider, x, y, cells);
        addCellIfAbsent(collision, collider, x, y + 1, cells);
        addCellIfAbsent(collision, collider, x, y + 2, cells);
        addCellIfAbsent(collision, collider, x, y + 3, cells);
        
        // add activator
        DoorActivator activator = new DoorActivator(x, y + 2, cells,
                unlockedDoor, lockedDoor);
        activators.add(activator);
        trim.setCell(x, y + 2, activator.getCell());
        cells.clear();
    }
    
    private boolean inBounds(int x, int y, TiledMapTileLayer layer) {
        return x >= 0 && x < layer.getWidth() && y >= 0 && y < layer.getHeight();
    }
    
    private boolean hasCell(int x, int y, TiledMapTileLayer layer) {
        return inBounds(x, y, layer) && layer.getCell(x, y) != null;
    }
    
    private boolean isGround(int x, int y, TiledMapTileLayer layer) {
        return inBounds(x, y, layer) 
                && layer.getCell(x, y) != null && layer.getCell(x, y).getTile() == ground;
    }
    
    private boolean isWall(int x, int y, TiledMapTileLayer layer) {
        return inBounds(x, y, layer) 
                && layer.getCell(x, y) != null && layer.getCell(x, y).getTile() != ground;
    }

    public Vector2 getPoint(Rectangle rect) {
        int left = (int) rect.x * SCALE;
        int right = (int) (left + rect.width * SCALE);
        int top = (int) rect.y * SCALE;
        int bottom = (int) (top + rect.height * SCALE);
        return new Vector2(randomNumber(left + 1, right - 2), randomNumber(top + 1, bottom - 2));
    }

    private int randomNumber(int min, int max) {
        return rand.nextInt(max - min + 1) + min;
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
                for (int dx = 0; dx < SCALE; dx++) {
                    for (int dy = 0; dy < SCALE; dy++) {
                        int x = startX + dx;
                        int y = startY + dy;
                        if (inBounds(x, y, layer.getWidth(), layer.getHeight())) {
                            addCell(layer, tile, x, y);
                        }
                    }
                }
            }
        }
    }

    private boolean inBounds(int x, int y, int w, int h) {
        // leave room for walls
        return x > 0 && x < (w - 1) && y > 0 && y < (h - 3);
    }

    private Cell addCell(TiledMapTileLayer layer, TiledMapTile tile, int x, int y) {
        Cell cell = new Cell();
        cell.setTile(tile);
        layer.setCell(x, y, cell);
        return cell;
    }
    
    private void addCell(TiledMapTileLayer layer, TiledMapTile tile, int x, int y,
            List<RemovableCell> cells) {
        Cell cell = addCell(layer, tile, x, y);
        cells.add(new RemovableCell(cell, layer, x, y));
    }
    
    private void addCellIfAbsent(TiledMapTileLayer layer, TiledMapTile tile, int x, int y,
            List<RemovableCell> cells) {
        if (layer.getCell(x, y) == null) {
            addCell(layer, tile, x, y, cells);
        }
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
                    if (l.width > MAX_LEAF_SIZE || l.height > MAX_LEAF_SIZE || Math.random() < 0.75) {
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
