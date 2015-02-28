package com.eldritch.invoken.encounter.proc;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.imageio.ImageIO;

import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.type.CoverPoint;
import com.eldritch.invoken.encounter.ConnectedRoom;
import com.eldritch.invoken.encounter.ConnectedRoom.Type;
import com.eldritch.invoken.encounter.ConnectedRoomManager;
import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.encounter.NaturalVector2;
import com.eldritch.invoken.encounter.layer.EncounterLayer;
import com.eldritch.invoken.encounter.layer.LocationCell;
import com.eldritch.invoken.encounter.layer.LocationLayer;
import com.eldritch.invoken.encounter.layer.LocationLayer.CollisionLayer;
import com.eldritch.invoken.encounter.layer.LocationMap;
import com.eldritch.invoken.encounter.proc.BspGenerator.CellType;
import com.eldritch.invoken.encounter.proc.EncounterGenerator.EncounterRoom;
import com.eldritch.invoken.gfx.Light;
import com.eldritch.invoken.gfx.NormalMappedTile;
import com.eldritch.invoken.proto.Locations.Biome;
import com.eldritch.invoken.proto.Locations.Encounter;
import com.eldritch.invoken.proto.Locations.Encounter.ActorParams.ActorScenario;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.screens.GameScreen.GameState;
import com.eldritch.invoken.util.Settings;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class LocationGenerator {
    // string constants required for all biome types
    private static final String FLOOR = "/floor";
    private static final String ROOF = "/roof";
    private static final String MID_WALL_TOP = "/mid-wall-top";
    // private static final String MID_WALL_CENTER = "/mid-wall-center";
    private static final String MID_WALL_BOTTOM = "/mid-wall-bottom";
    private static final String LEFT_TRIM = "/left-trim";
    private static final String RIGHT_TRIM = "/right-trim";
    private static final String TOP_LEFT_TRIM = "/top-left-trim";
    private static final String TOP_RIGHT_TRIM = "/top-right-trim";
    private static final String LEFT_CORNER = "/left-corner";
    private static final String RIGHT_CORNER = "/right-corner";
    private static final String OVERLAY_BELOW_TRIM = "/overlay-below-trim";
    private static final String OVERLAY_LEFT_TRIM = "/overlay-left-trim";
    private static final String OVERLAY_RIGHT_TRIM = "/overlay-right-trim";
    private static final String COLLISION = "markers/collision";

    private static final int PX = Settings.PX;
    private static final int SCALE = 1;
    private final long globalSeed;
    private Random rand;

    private final GameState state;
    private final String biome;
    private final TextureAtlas atlas;
    private final TextureAtlas normalAtlas;

    private final TiledMapTile ground;
    private final TiledMapTile midWallTop;
    // private final TiledMapTile midWallCenter;
    private final TiledMapTile midWallBottom;

    private final NormalMappedTile leftTrim;
    private final NormalMappedTile rightTrim;
    private final TiledMapTile narrowWall;
    private final TiledMapTile narrowTop;
    private final TiledMapTile collider;
    private final TiledMapTile shortCollider;

    public LocationGenerator(GameState state, Biome biomeType, long seed) {
        this.state = state;
        this.globalSeed = seed;
        this.biome = biomeType.name().toLowerCase();
        atlas = GameScreen.ATLAS;
        normalAtlas = GameScreen.NORMAL_ATLAS;

        ground = getTile(FLOOR);

        midWallTop = getTile(MID_WALL_TOP);
        // midWallCenter = new StaticTiledMapTile(atlas.findRegion(biome +
        // MID_WALL_CENTER));
        midWallBottom = getTile(MID_WALL_BOTTOM);

        leftTrim = getTile(LEFT_TRIM);
        rightTrim = getTile(RIGHT_TRIM);
        narrowWall = merge(rightTrim, leftTrim);

        NormalMappedTile topLeft = getTile(TOP_LEFT_TRIM);
        NormalMappedTile topRight = getTile(TOP_RIGHT_TRIM);
        narrowTop = merge(topLeft, topRight);

        collider = new StaticTiledMapTile(atlas.findRegion(COLLISION));
        shortCollider = new StaticTiledMapTile(atlas.findRegion(COLLISION));
        shortCollider.getProperties().put("collision", "short");
    }

    private NormalMappedTile getTile(String asset) {
        return new NormalMappedTile(atlas.findRegion(biome + asset), normalAtlas.findRegion(biome
                + asset));
    }

    public Location generate(com.eldritch.invoken.proto.Locations.Location proto) {
        // generate a new random seed that combines the global player seed with the location name
        long seed = globalSeed ^ proto.getId().hashCode();
        System.out.println("global seed: " + globalSeed);
        System.out.println("hash code: " + proto.getId().hashCode());
        System.out.println("seed: " + seed);
        this.rand = new Random(seed);

        int roomCount = 1;
        for (Encounter encounter : proto.getEncounterList()) {
            int count = 1;
            if (!encounter.getUnique()) {
                count += (int) (rand.nextDouble() * 3);
            }
            roomCount += count;
        }
        EncounterGenerator bsp = new EncounterGenerator(roomCount * 2, proto.getEncounterList(),
                seed);
        NaturalVector2.init(bsp.getWidth(), bsp.getHeight());

        bsp.generateSegments();
        bsp.save("bsp");
        CellType[][] typeMap = bsp.getMap();

        // create map
        int width = bsp.getWidth();
        int height = bsp.getHeight();
        LocationMap map = getBaseMap(width, height);

        // create layers
        InvokenGame.log("Creating Base");
        LocationLayer base = createBaseLayer(typeMap, width, height, map);
        map.getLayers().add(base);

        InvokenGame.log("Creating Trim");
        LocationLayer trim = createEmptyLayer(base, map, "trim");
        map.getLayers().add(trim);

        InvokenGame.log("Creating Overlay");
        LocationLayer overlay = createOverlayLayer(base, map);
        LocationLayer roof = createEmptyLayer(base, map, "roof");

        InvokenGame.log("Creating Collision");
        CollisionLayer collision = createCollisionLayer(base, map);
        map.getLayers().add(collision);

        InvokenGame.log("Creating Roof");
        TiledMapTile roofTile = getTile(ROOF);
        for (int i = 0; i < typeMap.length; i++) {
            for (int j = 0; j < typeMap[i].length; j++) {
                if (typeMap[i][j] != CellType.Floor) {
                    addTile(i, j, roof, roofTile);
                }
            }
        }

        InvokenGame.log("Creating Overlays");
        LocationLayer overlayTrim1 = createTrimLayer(base, overlay, map);
        LocationLayer doorLayer = createEmptyLayer(base, map, "doors");
        List<LocationLayer> overlayTrims = createOverlayTrimLayer(base, roof, overlay, map);

        // add all the overlays
        map.addOverlay(roof);
        map.addOverlay(overlayTrim1);
        map.addOverlay(overlay);
        map.addOverlay(doorLayer);
        for (LocationLayer layer : overlayTrims) {
            map.addOverlay(layer);
        }

        InvokenGame.log("Connecting Rooms");
        ConnectedRoomManager rooms = createRooms(bsp.getEncounterRooms(), typeMap);
        map.setRooms(rooms);
        save(rooms.getGrid(), "connected-rooms");

        InvokenGame.log("Adding Furniture");
        RoomGenerator roomGenerator = new RoomGenerator(map, seed);
        roomGenerator.generate(rooms);

        InvokenGame.log("Creating Spawn Layers");
        for (LocationLayer layer : createSpawnLayers(base, collision, bsp, map)) {
            map.getLayers().add(layer);
        }

        // add furniture
        // InvokenGame.log("Adding Furniture");
        // List<Activator> activators = new ArrayList<Activator>();
        IcarianFurnitureGenerator furnitureGenerator = new IcarianFurnitureGenerator(atlas, ground,
                seed);

        // doors
        InvokenGame.log("Adding Doors");
        furnitureGenerator.createDoors(rooms, base, map.getActivators());

        // lights
        InvokenGame.log("Adding Lights");
        List<Light> lights = new ArrayList<Light>();
        furnitureGenerator.addLights(trim, base, lights, midWallTop);

        // clutter
        InvokenGame.log("Adding Clutter");
        // map.getLayers().add(furnitureGenerator.generateClutter(base, map));

        // add cover points now that all collidable furniture has been placed
        map.addAllCover(getCover(base, collision));

        Location location = new Location(proto, map, state, globalSeed);
        location.addLights(lights);
        // location.addActivators(activators);
        location.addActivators(map.getActivators());

        // debug
        // saveLayer(base);

        return location;
    }

    private List<CoverPoint> getCover(LocationLayer base, CollisionLayer collision) {
        List<CoverPoint> cover = new ArrayList<CoverPoint>();
        for (int x = 0; x < collision.getWidth(); x++) {
            for (int y = 0; y < collision.getHeight(); y++) {
                if (collision.getCell(x, y) != null) {
                    // collision tile, check the area around for ground
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = -1; dy <= 1; dy++) {
                            if ((dx == 0) != (dy == 0)) {
                                // 4 cardinal directions, so one delta must be 0, but not both
                                if (openGround(x + dx, y + dy, base, collision)
                                        && hasOpenNeighbor(x, y, dx, dy, base, collision)) {
                                    // now check the points along the perpendicular from the
                                    // collision tile
                                    cover.add(new CoverPoint(new Vector2(x + dx + 0.5f, y + dy
                                            + 0.5f)));
                                }
                            }
                        }
                    }
                }
            }
        }
        return cover;
    }

    private boolean hasOpenNeighbor(int x, int y, int dx, int dy, LocationLayer base,
            LocationLayer collision) {
        if (Math.abs(dx) > 0) {
            // check points above and below the origin
            return openGround(x, y + 1, base, collision) || openGround(x, y - 1, base, collision);
        } else if (Math.abs(dy) > 0) {
            // check points left and right of the origin
            return openGround(x + 1, y, base, collision) || openGround(x - 1, y, base, collision);
        } else {
            return false;
        }
    }

    private boolean openGround(int x, int y, LocationLayer layer, LocationLayer collision) {
        return layer.isGround(x, y) && collision.getCell(x, y) == null;
    }

    private ConnectedRoomManager createRooms(Collection<EncounterRoom> chambers,
            CellType[][] typeMap) {
        ConnectedRoomManager rooms = new ConnectedRoomManager(typeMap.length, typeMap[0].length);

        InvokenGame.log("Create Chambers");
        ImmutableBiMap.Builder<EncounterRoom, ConnectedRoom> mapping = new ImmutableBiMap.Builder<EncounterRoom, ConnectedRoom>();
        for (EncounterRoom encounter : chambers) {
            List<NaturalVector2> points = new ArrayList<NaturalVector2>();

            // boundary of the chamber
            Rectangle rect = encounter.getBounds();
            int startX = (int) rect.x;
            int endX = (int) (rect.x + rect.width);
            int startY = (int) rect.y;
            int endY = (int) (rect.y + rect.height);

            // the endpoints are exclusive, as a rectangle at (0, 0) with size
            // (1, 1) should cover
            // only rooms[0][0], not rooms[1][1]
            for (int x = startX; x < endX; x++) {
                for (int y = startY; y < endY; y++) {
                    if (typeMap[x][y] == CellType.Floor) {
                        points.add(NaturalVector2.of(x, y));
                    }
                }
            }

            // rooms generated by the BSP are chambers
            ConnectedRoom room = new ConnectedRoom(Type.Chamber, points);
            for (NaturalVector2 point : points) {
                rooms.setRoom(point.x, point.y, room);
            }
            mapping.put(encounter, room);
        }
        rooms.setMapping(mapping.build());

        // all chambers are identified, so any remaining floor points belong to
        // hallways
        InvokenGame.log("Create Hallways");
        for (int x = 0; x < typeMap.length; x++) {
            for (int y = 0; y < typeMap[x].length; y++) {
                if (typeMap[x][y] == CellType.Floor && !rooms.hasRoom(x, y)) {
                    // create a new hall and flood fill the neighbors
                    Set<NaturalVector2> points = new LinkedHashSet<NaturalVector2>();
                    fillHall(points, x, y, typeMap, rooms);

                    ConnectedRoom room = new ConnectedRoom(Type.Hall, points);
                    for (NaturalVector2 point : points) {
                        rooms.setRoom(point.x, point.y, room);
                    }
                }
            }
        }

        // finally, connect the rooms together
        InvokenGame.log("Connect Chambers and Hallways");
        Set<NaturalVector2> visited = new LinkedHashSet<NaturalVector2>();
        for (int x = 0; x < typeMap.length; x++) {
            for (int y = 0; y < typeMap[x].length; y++) {
                NaturalVector2 current = NaturalVector2.of(x, y);
                if (!visited.contains(current)) {
                    if (rooms.hasRoom(x, y)) {
                        fillNeighbors(current, rooms, visited);
                    }
                }
            }
        }

        return rooms;
    }

    private void fillHall(Set<NaturalVector2> points, int seedX, int seedY, CellType[][] typeMap,
            ConnectedRoomManager rooms) {
        LinkedList<NaturalVector2> queue = new LinkedList<NaturalVector2>();
        NaturalVector2 seed = NaturalVector2.of(seedX, seedY);
        queue.add(seed);
        while (!queue.isEmpty()) {
            NaturalVector2 current = queue.remove();
            int x = current.x;
            int y = current.y;

            if (typeMap[x][y] == CellType.Floor && !rooms.hasRoom(x, y)
                    && !points.contains(current)) {
                points.add(current);

                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        if (dx == 0 && dy == 0) {
                            continue;
                        }

                        if (inBounds(x + dx, y + dy, rooms.getGrid())) {
                            queue.add(NaturalVector2.of(x + dx, y + dy));
                        }
                    }
                }
            }
        }
    }

    private void fillNeighbors(NaturalVector2 seed, ConnectedRoomManager rooms,
            Set<NaturalVector2> visited) {
        LinkedList<NaturalVector2> queue = new LinkedList<NaturalVector2>();
        queue.add(seed);
        while (!queue.isEmpty()) {
            NaturalVector2 current = queue.remove();
            int x = current.x;
            int y = current.y;

            if (rooms.hasRoom(x, y) && !visited.contains(current)) {
                visited.add(current);

                ConnectedRoom currentRoom = rooms.getRoom(x, y);
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        if (dx == 0 && dy == 0) {
                            continue;
                        }

                        if (inBounds(x + dx, y + dy, rooms.getGrid())
                                && rooms.hasRoom(x + dx, y + dy)) {
                            queue.add(NaturalVector2.of(x + dx, y + dy));

                            ConnectedRoom nextRoom = rooms.getRoom(x + dx, y + dy);
                            if (currentRoom != nextRoom) {
                                // neighbor in a different room, so it's connected
                                currentRoom.addNeighbor(nextRoom);
                                nextRoom.addNeighbor(currentRoom);
                            }
                        }
                    }
                }
            }
        }
    }

    private <T> boolean inBounds(int x, int y, T[][] grid) {
        return x >= 0 && x < grid.length && y >= 0 && y < grid[x].length;
    }

    private LocationMap getBaseMap(int width, int height) {
        LocationMap map = new LocationMap(ground, width, height);
        MapProperties mapProperties = map.getProperties();
        mapProperties.put("width", width);
        mapProperties.put("height", height);
        mapProperties.put("tilewidth", PX);
        mapProperties.put("tileheight", PX);
        return map;
    }

    private LocationLayer createBaseLayer(CellType[][] typeMap, int width, int height,
            LocationMap map) {
        LocationLayer layer = new LocationLayer(width, height, PX, PX, map);
        layer.setVisible(true);
        layer.setOpacity(1.0f);
        layer.setName("base");

        for (int i = 0; i < typeMap.length; i++) {
            for (int j = 0; j < typeMap[i].length; j++) {
                if (typeMap[i][j] == CellType.Floor) {
                    addTile(i, j, layer, ground);
                }
            }
        }

        // add walls
        addWalls(layer);
        InvokenGame.log("done");

        return layer;
    }

    private LocationLayer createEmptyLayer(LocationLayer base, LocationMap map, String name) {
        LocationLayer layer = new LocationLayer(base.getWidth(), base.getHeight(), PX, PX, map);
        layer.setVisible(true);
        layer.setOpacity(1.0f);
        layer.setName(name);
        return layer;
    }

    private LocationLayer createTrimLayer(LocationLayer base, LocationLayer overlay, LocationMap map) {
        LocationLayer layer = new LocationLayer(base.getWidth(), base.getHeight(), PX, PX, map);
        layer.setVisible(true);
        layer.setOpacity(1.0f);
        layer.setName("overlay-trim-1");

        // fill in sides
        for (int x = 0; x < base.getWidth(); x++) {
            for (int y = 0; y < base.getHeight(); y++) {
                if (base.getCell(x, y) == null) {
                    boolean leftGround = x - 1 >= 0 && base.getCell(x - 1, y) != null
                            && overlay.getCell(x - 1, y) == null;
                    boolean rightGround = x + 1 < base.getWidth() && base.getCell(x + 1, y) != null
                            && overlay.getCell(x + 1, y) == null;
                    if (leftGround) {
                        if (rightGround) {
                            // narrow wall
                            addCell(layer, narrowWall, x, y);
                        } else {
                            // right trim
                            addCell(layer, rightTrim, x, y);
                        }
                    } else if (rightGround) {
                        // left trim
                        addCell(layer, leftTrim, x, y);
                    }
                }
            }
        }

        return layer;
    }

    private LocationLayer createOverlayLayer(LocationLayer base, LocationMap map) {
        LocationLayer layer = new LocationLayer(base.getWidth(), base.getHeight(), PX, PX, map);
        layer.setVisible(true);
        layer.setOpacity(1.0f);
        layer.setName("overlay");

        TiledMapTile belowTrim = getTile(OVERLAY_BELOW_TRIM);
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

    private List<LocationLayer> createOverlayTrimLayer(LocationLayer base, LocationLayer roof,
            LocationLayer overlay, LocationMap map) {
        LocationLayer layer1 = new LocationLayer(base.getWidth(), base.getHeight(), PX, PX, map);
        layer1.setVisible(true);
        layer1.setOpacity(1.0f);
        layer1.setName("overlay-trim-2");

        TiledMapTile overlayLeftTrim = getTile(OVERLAY_LEFT_TRIM);
        TiledMapTile overlayRightTrim = getTile(OVERLAY_RIGHT_TRIM);

        // fill in sides
        for (int x = 0; x < overlay.getWidth(); x++) {
            for (int y = 0; y < overlay.getHeight(); y++) {
                Cell cell = overlay.getCell(x, y);
                if (cell != null) {
                    boolean lGround = base.isFilled(x - 1, y) && overlay.getCell(x - 1, y) == null;
                    boolean rGround = base.isFilled(x + 1, y) && overlay.getCell(x + 1, y) == null;
                    if (lGround) {
                        if (rGround) {
                            // narrow top
                            addCell(layer1, narrowTop, x, y);
                        } else {
                            // left space is ground
                            addCell(layer1, overlayRightTrim, x, y);
                        }
                    } else if (rGround) {
                        // right space is ground
                        addCell(layer1, overlayLeftTrim, x, y);
                    }
                }
            }
        }

        // bottom left
        TiledMapTile leftCorner = getTile(LEFT_CORNER);

        // bottom right
        TiledMapTile rightCorner = getTile(RIGHT_CORNER);

        // required offsets
        leftCorner.setOffsetX(Settings.PX / 2);
        leftCorner.setOffsetY(Settings.PX / 2);
        rightCorner.setOffsetY(Settings.PX / 2);

        // fill in corners
        for (int x = 0; x < roof.getWidth(); x++) {
            for (int y = 0; y < roof.getHeight(); y++) {
                if (roof.isFilled(x, y) && !layer1.isFilled(x, y) && !overlay.isFilled(x, y)) {
                    if (overlay.isFilled(x + 1, y)) {
                        // case I: overlay to the right, wall at current position
                        // add a left corner here
                        addCell(layer1, leftCorner, x, y);
                    }
                }
            }
        }

        LocationLayer layer2 = new LocationLayer(base.getWidth(), base.getHeight(), PX, PX, map);
        layer2.setVisible(true);
        layer2.setOpacity(1.0f);
        layer2.setName("overlay-trim-3");

        for (int x = 0; x < roof.getWidth(); x++) {
            for (int y = 0; y < roof.getHeight(); y++) {
                if (roof.isFilled(x, y) && !layer2.isFilled(x, y) && !overlay.isFilled(x, y)) {
                    if (overlay.isFilled(x - 1, y)) {
                        // case II: overlay to the left, wall at current position
                        // add a right corner here
                        addCell(layer2, rightCorner, x, y);
                    }
                }
            }
        }

        return ImmutableList.of(layer1, layer2);
    }

    private CollisionLayer createCollisionLayer(LocationLayer base, LocationMap map) {
        CollisionLayer layer = new CollisionLayer(base.getWidth(), base.getHeight(), PX, PX, map);
        layer.setVisible(false);
        layer.setOpacity(1.0f);
        layer.setName("collision");

        for (int x = 0; x < base.getWidth(); x++) {
            for (int y = 0; y < base.getHeight(); y++) {
                Cell cell = base.getCell(x, y);
                if (cell == null || cell.getTile() != ground) {
                    TiledMapTile tile = collider;
                    if (y > 0 && base.isGround(x, y - 1)) {
                        // ground below, so use the low collider
                        tile = shortCollider;
                    }

                    // non-empty, non-ground space
                    addCell(layer, tile, x, y);
                    map.setWall(x, y);
                }
            }
        }

        return layer;
    }

    private List<LocationLayer> createSpawnLayers(LocationLayer base, LocationLayer collision,
            EncounterGenerator generator, LocationMap map) {
        List<LocationLayer> layers = new ArrayList<LocationLayer>();
        LocationLayer playerLayer = null;
        for (EncounterRoom encounterRoom : generator.getEncounterRooms()) {
            Encounter encounter = encounterRoom.getEncounter();
            Rectangle bounds = encounterRoom.getRestrictedBounds();

            if (encounter.getOrigin()) {
                playerLayer = new LocationLayer(base.getWidth(), base.getHeight(), PX, PX, map);
                playerLayer.setVisible(false);
                playerLayer.setOpacity(1.0f);
                playerLayer.setName("player");

                List<NaturalVector2> freeSpaces = getFreeSpaces(collision, bounds);
                Collections.shuffle(freeSpaces, rand);
                NaturalVector2 position = Iterables.getFirst(freeSpaces, null);
                if (position != null) {
                    addCell(playerLayer, collider, position.x, position.y);
                } else {
                    // TODO: this should never happen, but just in case we should regenerate the
                    // whole map
                }

                layers.add(playerLayer);
            } else {
                LocationLayer layer = createLayer(encounter, bounds, base, collision, map);
                layers.add(layer);
            }
        }

        return layers;
    }

    private LocationLayer createLayer(Encounter encounter, Rectangle room, LocationLayer base,
            LocationLayer collision, LocationMap map) {
        LocationLayer layer = new EncounterLayer(encounter, base.getWidth(), base.getHeight(), PX,
                PX, map);
        layer.setVisible(false);
        layer.setOpacity(1.0f);
        layer.setName("encounter-" + room.getX() + "-" + room.getY());

        // enemy placement can be non-deterministic between loads
        List<NaturalVector2> freeSpaces = getFreeSpaces(collision, room);
        Collections.shuffle(freeSpaces);

        Iterator<NaturalVector2> it = freeSpaces.iterator();
        for (ActorScenario scenario : encounter.getActorParams().getActorScenarioList()) {
            for (int i = 0; i < scenario.getMax(); i++) {
                NaturalVector2 position = it.hasNext() ? it.next() : getPoint(room, base, layer);
                addCell(layer, collider, position.x, position.y);
            }
        }

        return layer;
    }

    public static List<NaturalVector2> getFreeSpaces(LocationLayer layer, Rectangle bounds) {
        List<NaturalVector2> freeSpaces = new ArrayList<NaturalVector2>();
        for (int x = (int) bounds.x; x < bounds.x + bounds.width; x++) {
            for (int y = (int) bounds.y; y < bounds.y + bounds.height; y++) {
                Cell cell = layer.getCell(x, y);
                if (cell == null) {
                    freeSpaces.add(NaturalVector2.of(x, y));
                }
            }
        }
        return freeSpaces;
    }

    public static void sortByWeight(List<Encounter> encounters) {
        // unique encounters appear first, then ordered by descending weight
        Collections.sort(encounters, new Comparator<Encounter>() {
            @Override
            public int compare(Encounter e1, Encounter e2) {
                if (e1.getUnique() && !e2.getUnique()) {
                    return -1;
                }
                if (e2.getUnique() && !e1.getUnique()) {
                    return 1;
                }

                double diff = e1.getWeight() - e2.getWeight();
                return diff > 0 ? -1 : diff < 0 ? 1 : 0;
            }
        });
    }

    private void addWalls(LocationLayer layer) {
        for (int x = 0; x < layer.getWidth(); x++) {
            for (int y = 0; y < layer.getHeight(); y++) {
                Cell cell = layer.getCell(x, y);
                if (cell != null && cell.getTile() == ground) {
                    // check for empty space above
                    if (y + 2 < layer.getHeight() && layer.getCell(x, y + 2) == null) {
                        addCell(layer, midWallBottom, x, y + 0);
                        // addCell(layer, midWallCenter, x, y + 1);
                        addCell(layer, midWallTop, x, y + 1);
                    }
                }
            }
        }
    }

    public NaturalVector2 getPoint(Rectangle rect, LocationLayer base, LocationLayer layer) {
        int left = (int) rect.x * SCALE;
        int right = (int) (left + rect.width * SCALE);
        int top = (int) rect.y * SCALE;
        int bottom = (int) (top + rect.height * SCALE);
        NaturalVector2 seed = NaturalVector2.of(randomNumber(left + 1, right - 2),
                randomNumber(top + 1, bottom - 2));
        return getPoint(rect, base, layer, seed);
    }

    public NaturalVector2 getPoint(Rectangle rect, LocationLayer base, LocationLayer layer,
            NaturalVector2 seed) {
        Set<NaturalVector2> visited = new LinkedHashSet<NaturalVector2>();
        LinkedList<NaturalVector2> queue = new LinkedList<NaturalVector2>();

        queue.add(seed);
        visited.add(seed);
        while (!queue.isEmpty()) {
            NaturalVector2 point = queue.remove();
            if (base.isGround(point.x, point.y) && !layer.hasCell(point.x, point.y)) {
                // valid point: ground and no pre-existing spawn nodes
                return point;
            }

            // bad point, so try all neighbors in breadth-first expansion
            int x1 = (int) rect.x * SCALE;
            int x2 = (int) (x1 + rect.width * SCALE);
            int y1 = (int) rect.y * SCALE;
            int y2 = (int) (y1 + rect.height * SCALE);
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0) {
                        continue;
                    }

                    int x = point.x + dx;
                    int y = point.y + dy;
                    NaturalVector2 neighbor = NaturalVector2.of(x, y);
                    if (x > x1 && x < x2 && y > y1 && y < y2 && !visited.contains(neighbor)) {
                        visited.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
        }

        // this should never happen
        return seed;
    }

    private int randomNumber(int min, int max) {
        return rand.nextInt(max - min + 1) + min;
    }

    private void addTile(int i, int j, LocationLayer layer, TiledMapTile tile) {
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

    private boolean inBounds(int x, int y, int w, int h) {
        // leave room for walls
        return x > 0 && x < (w - 1) && y > 0 && y < (h - 3);
    }

    private static Cell addCell(LocationLayer layer, TiledMapTile tile, int x, int y) {
        Cell cell = new LocationCell(NaturalVector2.of(x, y), layer);
        cell.setTile(tile);
        layer.setCell(x, y, cell);
        return cell;
    }

    public TextureAtlas getAtlas() {
        return atlas;
    }

    private static NormalMappedTile merge(NormalMappedTile left, NormalMappedTile right) {
        TextureRegion diffuse = merge(left.getTextureRegion(), right.getTextureRegion());
        TextureRegion normal = merge(left.getNormalRegion(), right.getNormalRegion());
        return new NormalMappedTile(diffuse, normal);
    }

    private static TextureRegion merge(TextureRegion left, TextureRegion right) {
        FrameBuffer buffer = new FrameBuffer(Format.RGB888, Settings.PX, Settings.PX, false);
        TextureRegion region = new TextureRegion(buffer.getColorBufferTexture());
        region.flip(false, true);

        // extract the part of each region we care about
        int size = Settings.PX / 2;
        TextureRegion leftPart = new TextureRegion(left, 0, 0, size, Settings.PX);
        TextureRegion rightPart = new TextureRegion(right, size, 0, size, Settings.PX);

        // setup the projection matrix
        buffer.begin();
        SpriteBatch batch = new SpriteBatch();
        Matrix4 m = new Matrix4();
        m.setToOrtho2D(0, 0, buffer.getWidth(), buffer.getHeight());
        batch.setProjectionMatrix(m);

        // draw to the frame buffer
        batch.begin();
        batch.draw(leftPart, 0, 0, leftPart.getRegionWidth(), leftPart.getRegionHeight());
        batch.draw(rightPart, size, 0, rightPart.getRegionWidth(), rightPart.getRegionHeight());
        batch.end();
        buffer.end();

        return region;
    }

    private void saveLayer(LocationLayer layer) {
        BufferedImage image = new BufferedImage(layer.getWidth(), layer.getHeight(),
                BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < layer.getWidth(); x++) {
            for (int y = 0; y < layer.getHeight(); y++) {
                Cell cell = layer.getCell(x, layer.getHeight() - y - 1);
                if (layer.isGround(cell)) {
                    image.setRGB(x, y, 0xffffff);
                } else {
                    image.setRGB(x, y, 0);
                }
            }
        }

        File outputfile = new File(System.getProperty("user.home") + "/ground.png");
        try {
            ImageIO.write(image, "png", outputfile);
        } catch (IOException e) {
            InvokenGame.error("Failed saving level image!", e);
        }
    }

    public <T> void save(T[][] grid, String filename) {
        int width = grid.length;
        int height = grid[0].length;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // every element has a unique color in the grid
        Random rand = new Random();
        Map<T, Color> colors = new HashMap<T, Color>();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                T elem = grid[x][height - y - 1]; // images have (0, 0) in upper-left
                if (!colors.containsKey(elem)) {
                    if (elem == null) {
                        colors.put(elem, Color.BLACK);
                    } else {
                        // color takes 3 floats, from 0 to 1
                        float r = rand.nextFloat();
                        float g = rand.nextFloat();
                        float b = rand.nextFloat();
                        colors.put(elem, new Color(r, g, b));
                    }
                }

                image.setRGB(x, y, colors.get(elem).getRGB());
            }
        }

        File outputfile = new File(System.getProperty("user.home") + "/" + filename + ".png");
        try {
            ImageIO.write(image, "png", outputfile);
        } catch (IOException e) {
            InvokenGame.error("Failed saving grid image!", e);
        }
    }
}
