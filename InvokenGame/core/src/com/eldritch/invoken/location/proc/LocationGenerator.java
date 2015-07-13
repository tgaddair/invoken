package com.eldritch.invoken.location.proc;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import com.eldritch.invoken.actor.type.FixedPoint;
import com.eldritch.invoken.gfx.Light;
import com.eldritch.invoken.gfx.NormalMappedTile;
import com.eldritch.invoken.location.ConnectedRoom;
import com.eldritch.invoken.location.ConnectedRoomManager;
import com.eldritch.invoken.location.EncounterDescription;
import com.eldritch.invoken.location.EncounterDescription.AgentDescription;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.location.NaturalVector2;
import com.eldritch.invoken.location.ConnectedRoom.Type;
import com.eldritch.invoken.location.layer.LocationCell;
import com.eldritch.invoken.location.layer.LocationLayer;
import com.eldritch.invoken.location.layer.LocationMap;
import com.eldritch.invoken.location.layer.LocationLayer.CollisionLayer;
import com.eldritch.invoken.location.proc.BspGenerator.CellType;
import com.eldritch.invoken.location.proc.RoomGenerator.ControlRoom;
import com.eldritch.invoken.location.proc.WallTileMap.WallTile;
import com.eldritch.invoken.proto.Locations;
import com.eldritch.invoken.proto.Locations.Biome;
import com.eldritch.invoken.proto.Locations.ControlPoint;
import com.eldritch.invoken.proto.Locations.Encounter;
import com.eldritch.invoken.proto.Locations.Encounter.ActorParams.ActorScenario;
import com.eldritch.invoken.proto.Locations.Room;
import com.eldritch.invoken.proto.Locations.Territory;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.util.GameTransition;
import com.eldritch.invoken.util.Heuristics;
import com.eldritch.invoken.util.Settings;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class LocationGenerator {
    // string constants required for all biome types
    private static final String FLOOR = "/floor";
    private static final String ROOF = "/roof";
    private static final String WALL = "/wall";
    private static final String COLLISION = "markers/collision";

    private static final int PX = Settings.PX;
    private static final int SCALE = 1;
    private final long globalSeed;
    private Random rand;
    private long counter = 0;
    private int floor;

    private final GameTransition state;
    private final String biome;
    private final TextureAtlas atlas;
    private final TextureAtlas normalAtlas;

    private final WallTileMap walls;
    private final TiledMapTile ground;

    private final TiledMapTile narrowWall;
    private final TiledMapTile narrowTop;
    private final TiledMapTile collider;
    private final TiledMapTile shortCollider;

    public LocationGenerator(GameTransition state, Biome biomeType, long seed) {
        this.state = state;
        this.globalSeed = seed;
        this.biome = biomeType.name().toLowerCase();
        atlas = GameScreen.ATLAS;
        normalAtlas = GameScreen.NORMAL_ATLAS;

        String biomeName = "office";
        NormalMappedTile wall = getTile(WALL, biomeName);
        NormalMappedTile roof = getTile(ROOF, biomeName);
        walls = WallTileMap.from(wall, roof);
        ground = getTile(FLOOR, biome);

        narrowWall = merge(walls.getTile(WallTile.RightTrim), walls.getTile(WallTile.LeftTrim));

        NormalMappedTile topLeft = walls.getTile(WallTile.TopLeftTrim);
        NormalMappedTile topRight = walls.getTile(WallTile.TopRightTrim);
        narrowTop = merge(topLeft, topRight);

        collider = new StaticTiledMapTile(atlas.findRegion(COLLISION));
        shortCollider = new StaticTiledMapTile(atlas.findRegion(COLLISION));
        shortCollider.getProperties().put("collision", "short");
    }

    private NormalMappedTile getTile(String asset, String biome) {
        return new NormalMappedTile(atlas.findRegion(biome + asset), normalAtlas.findRegion(biome
                + asset));
    }

    public Level generate() {
        return generate(ImmutableList.<Locations.Location> of(), Settings.START_FLOOR);
    }

    public Level generate(List<Locations.Location> protos, int floor) {
        System.out.println("global seed: " + globalSeed);

        // System.out.println("hash code: " + proto.getId().hashCode());
        // long hashCode = proto.getId().hashCode();
        long hashCode = floor;

        final int attempts = 3;
        while (counter < attempts) {
            // generate a new random seed that combines the global player seed
            // with the location
            // name
            long seed = (globalSeed ^ hashCode) + counter++;
            try {
                return generate(protos, floor, seed);
            } catch (Exception ex) {
                InvokenGame.error("Failed generating location: " + counter, ex);
            }
        }
        throw new IllegalStateException(String.format(
                "Failed to generate level %d after %d attempts", floor, attempts));
    }

    private Level generate(List<Locations.Location> protos, int floor, long seed) {
        System.out.println("seed: " + seed);
        this.rand = new Random(seed);
        this.floor = floor;

        // territory
        List<Territory> territory = new ArrayList<>();

        // control points, both generic and from locations
        List<ControlPoint> controlPoints = new ArrayList<>();
        controlPoints.add(ControlPointGenerator.generateOrigin(floor));
        controlPoints.add(ControlPointGenerator.generate(17, 20));
        controlPoints.add(ControlPointGenerator.generateExit(floor));

        RoomGenerator bsp = RoomGenerator.from(territory, controlPoints, seed);
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
        List<LocationLayer> baseLayers = createBaseLayers(typeMap, width, height, map);
        LocationLayer base = Iterables.getFirst(baseLayers, null);
        for (LocationLayer layer : baseLayers) {
            map.getLayers().add(layer);
        }

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
        TiledMapTile roofTile = walls.getTile(WallTile.Roof);
        for (int i = 0; i < typeMap.length - 1; i++) {
            for (int j = 0; j < typeMap[i].length; j++) {
                if (typeMap[i][j] != CellType.Floor && typeMap[i + 1][j] != CellType.Floor) {
                    addTile(i, j, roof, roofTile);
                }
            }
        }

        InvokenGame.log("Creating Overlays");
        LocationLayer trimLayer = createTrimLayer(base, overlay, map);
        LocationLayer doorLayer = createEmptyLayer(base, map, "doors");
        List<LocationLayer> overlayTrims = createOverlayTrimLayer(base, roof, trimLayer, overlay,
                map);

        // add all the overlays
//        map.addOverlay(roof);
        map.addOverlay(trimLayer);
        map.addOverlay(overlay);
        map.addOverlay(doorLayer);
        for (LocationLayer layer : overlayTrims) {
            map.addOverlay(layer);
        }

        InvokenGame.log("Connecting Rooms");
        ConnectedRoomManager rooms = createRooms(bsp.getEncounterRooms(), typeMap);
        map.setRooms(rooms);
        save(rooms.getGrid(), "connected-rooms");

        InvokenGame.log("Claiming Territory");
        TerritoryGenerator territoryGen = new TerritoryGenerator(bsp, rooms, territory);
        territoryGen.claim();

        // load hallways
        List<Room> hallways = new ArrayList<>();
        // for (String id : proto.getHallIdList()) {
        // hallways.add(bsp.getRoom(id));
        // }
        // TODO: get generic hallways from the room selector

        InvokenGame.log("Adding Furniture");
        RoomDecorator roomDecorator = new RoomDecorator(map, seed);
        roomDecorator.generate(rooms, hallways);

        InvokenGame.log("Creating Spawn Layers");
        List<Encounter> encounters = InvokenGame.ENCOUNTER_SELECTOR.select(floor);
        for (LocationLayer layer : createSpawnLayers(base, collision, bsp, map, rooms, floor,
                encounters)) {
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
        // furnitureGenerator.addLights(trim, base, lights,
        // walls.getTile(WallTile.MidWallTop));

        // clutter
        InvokenGame.log("Adding Clutter");
        // map.getLayers().add(furnitureGenerator.generateClutter(base, map));

        // lock doors
        buildCriticalPath(rooms);
        lockDoors(rooms);

        // add cover points now that all collidable furniture has been placed
        map.addAllCover(getCover(base, collision));

        Locations.Level.Builder builder = Locations.Level.newBuilder();
        builder.setRegion(Settings.FIRST_REGION);
        builder.setLevel(floor);
        builder.addAllLocation(protos);

        Level level = new Level(builder.build(), map, state, globalSeed);
        level.addLights(lights);
        level.addLights(map.getLights());
        // location.addActivators(activators);
        level.addEntities(map);

        // debug
        // saveLayer(base);

        return level;
    }

    private static class ValuePath {
        private final List<ConnectedRoom> rooms = new ArrayList<>();
        private final int cost;

        public ValuePath() {
            this.cost = Integer.MAX_VALUE;
        }

        public ValuePath(List<ConnectedRoom> rooms, int cost) {
            this.rooms.addAll(rooms);
            this.cost = cost;
        }
    }

    private void buildCriticalPath(ConnectedRoomManager rooms) {
        Set<ConnectedRoom> visited = new HashSet<>();
        for (Entry<ControlRoom, ConnectedRoom> chamber : rooms.getChambers()) {
            if (chamber.getKey().getControlPoint().getOrigin()) {
                ConnectedRoom room = chamber.getValue();
                List<ConnectedRoom> active = new ArrayList<>();
                active.add(room);
                ValuePath best = exploreCriticalPath(rooms, room, active,
                        room.getInfo().getValue(), visited);
                for (ConnectedRoom criticalRooms : best.rooms) {
                    criticalRooms.setCriticalPath(true);
                }
                break;
            }
        }
    }

    private ValuePath exploreCriticalPath(ConnectedRoomManager rooms, ConnectedRoom room,
            List<ConnectedRoom> active, int cost, Set<ConnectedRoom> visited) {
        if (room.isChamber() && rooms.getControlRoom(room).getControlPoint().getExit()) {
            return new ValuePath(active, cost);
        }

        ValuePath bestPath = new ValuePath();
        for (ConnectedRoom neighbor : room.getNeighbors()) {
            if (!visited.contains(neighbor)) {
                visited.add(neighbor);
                active.add(neighbor);
                ValuePath path = exploreCriticalPath(rooms, neighbor, active, cost
                        + neighbor.getInfo().getValue(), visited);
                active.remove(active.size() - 1);
                if (path.cost < bestPath.cost) {
                    bestPath = path;
                }
            }
        }
        return bestPath;
    }

    private void lockDoors(ConnectedRoomManager rooms) {
        for (Entry<ControlRoom, ConnectedRoom> chamber : rooms.getChambers()) {
            ConnectedRoom room = chamber.getValue();
            Room info = room.getInfo();
            if (!room.onCriticalPath() && info.getValue() > 0
                    && rand.nextDouble() < room.getLockChance()) {
                room.lock(room.getLockStrength() + 1);
            }
        }
    }

    private List<FixedPoint> getCover(LocationLayer base, CollisionLayer collision) {
        List<FixedPoint> cover = new ArrayList<FixedPoint>();
        for (int x = 0; x < collision.getWidth(); x++) {
            for (int y = 0; y < collision.getHeight(); y++) {
                if (collision.getCell(x, y) != null) {
                    // collision tile, check the area around for ground
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = -1; dy <= 1; dy++) {
                            if ((dx == 0) != (dy == 0)) {
                                // 4 cardinal directions, so one delta must be
                                // 0, but not both
                                if (openGround(x + dx, y + dy, base, collision)
                                        && hasOpenNeighbor(x, y, dx, dy, base, collision)) {
                                    // now check the points along the
                                    // perpendicular from the
                                    // collision tile
                                    cover.add(new FixedPoint(new Vector2(x + dx + 0.5f, y + dy
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

    private ConnectedRoomManager createRooms(Collection<ControlRoom> chambers, CellType[][] typeMap) {
        ConnectedRoomManager rooms = new ConnectedRoomManager(typeMap.length, typeMap[0].length);

        InvokenGame.log("Create Chambers");
        ImmutableBiMap.Builder<ControlRoom, ConnectedRoom> mapping = new ImmutableBiMap.Builder<ControlRoom, ConnectedRoom>();
        for (ControlRoom encounter : chambers) {
            Set<NaturalVector2> points = new LinkedHashSet<NaturalVector2>();
            Set<NaturalVector2> chokePoints = new HashSet<NaturalVector2>();

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
                    NaturalVector2 point = NaturalVector2.of(x, y);
                    if (typeMap[x][y] == CellType.Floor) {
                        if (isChokePoint(point, typeMap, chokePoints)) {
                            // mark this point as a choke point so we don't
                            // place furniture
                            chokePoints.add(point);
                            chokePoints.add(NaturalVector2.of(point.x, point.y + 1));
                            chokePoints.add(NaturalVector2.of(point.x, point.y - 1));
                            points.remove(NaturalVector2.of(point.x, point.y - 1));
                        } else {
                            points.add(point);
                        }
                    }
                }
            }

            // rooms generated by the BSP are chambers
            ConnectedRoom room = new ConnectedRoom(encounter.getRoom(), Type.Chamber, points);
            for (NaturalVector2 point : points) {
                rooms.setRoom(point.x, point.y, room);
            }
            room.addChokePoints(chokePoints);
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

                    ConnectedRoom room = new ConnectedRoom(Room.getDefaultInstance(), Type.Hall,
                            points);
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
                                // neighbor in a different room, so it's
                                // connected
                                currentRoom.addNeighbor(nextRoom);
                                nextRoom.addNeighbor(currentRoom);
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isChokePoint(NaturalVector2 point, CellType[][] typeMap,
            Set<NaturalVector2> chokePoints) {
        // we already know this point is floor
        // return isVerticalChokePoint(point, typeMap)
        return chokePoints.contains(point)
                || isHorizontalChokePoint(NaturalVector2.of(point.x, point.y + 1), typeMap);
    }

    private boolean isVerticalChokePoint(NaturalVector2 point, CellType[][] typeMap) {
        // vertical -> the length of continuous floor points up and down is at
        // most two
        int verticalSum = 1;
        for (int dy = 1; point.y + dy < typeMap[point.x].length; dy++) {
            if (typeMap[point.x][point.y + dy] == CellType.Floor) {
                verticalSum++;
                if (verticalSum > 2) {
                    // too many vertical points
                    return false;
                }
            } else {
                // end of downward run
                break;
            }
        }

        for (int dy = -1; point.y + dy >= 0; dy--) {
            if (typeMap[point.x][point.y + dy] == CellType.Floor) {
                verticalSum++;
                if (verticalSum > 2) {
                    // too many vertical points
                    return false;
                }
            } else {
                // end of upward run
                break;
            }
        }

        return verticalSum <= 2;
    }

    private boolean isHorizontalChokePoint(NaturalVector2 point, CellType[][] typeMap) {
        // horizontal -> the length of continuous floor points left and right is
        // at most two
        int horizontalSum = 1;
        for (int dx = 1; point.x + dx < typeMap.length; dx++) {
            if (typeMap[point.x + dx][point.y] == CellType.Floor) {
                horizontalSum++;
                if (horizontalSum > 2) {
                    // too many horizontal points
                    return false;
                }
            } else {
                // end of rightward run
                break;
            }
        }

        for (int dx = -1; point.x + dx >= 0; dx--) {
            if (typeMap[point.x + dx][point.y] == CellType.Floor) {
                horizontalSum++;
                if (horizontalSum > 2) {
                    // too many horizontal points
                    return false;
                }
            } else {
                // end of leftward run
                break;
            }
        }

        return horizontalSum <= 2;
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

    private List<LocationLayer> createBaseLayers(CellType[][] typeMap, int width, int height,
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

        LocationLayer left = new LocationLayer(width, height, PX, PX, map);
        left.setVisible(true);
        left.setOpacity(1.0f);
        left.setName("base-left");

        LocationLayer right = new LocationLayer(width, height, PX, PX, map);
        right.setVisible(true);
        right.setOpacity(1.0f);
        right.setName("base-right");

        // add walls
        addWalls(layer, left, right, typeMap);
        InvokenGame.log("done");

        return ImmutableList.of(layer, left, right);
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
                            addCell(layer, walls.getTile(WallTile.RightTrim), x, y);
                        }
                    } else if (rightGround) {
                        // left trim
                        addCell(layer, walls.getTile(WallTile.LeftTrim), x, y);
                    }
                }
            }
        }

        return layer;
    }

    private boolean matchesTile(LocationLayer layer, int x, int y, TiledMapTile tile) {
        return layer.hasCell(x, y) && layer.getCell(x, y).getTile() == tile;
    }

    private LocationLayer createOverlayLayer(LocationLayer base, LocationMap map) {
        LocationLayer layer = new LocationLayer(base.getWidth(), base.getHeight(), PX, PX, map);
        layer.setVisible(true);
        layer.setOpacity(1.0f);
        layer.setName("overlay");

        TiledMapTile belowTrim = walls.getTile(WallTile.OverlayBelowTrim);
        // belowTrim.setOffsetY(Settings.PX / 2);
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
            LocationLayer trim, LocationLayer overlay, LocationMap map) {
        LocationLayer layer1 = new LocationLayer(base.getWidth(), base.getHeight(), PX, PX, map);
        layer1.setVisible(true);
        layer1.setOpacity(1.0f);
        layer1.setName("overlay-trim-2");

        TiledMapTile overlayLeftTrim = walls.getTile(WallTile.OverlayLeftTrim);
        TiledMapTile overlayRightTrim = walls.getTile(WallTile.OverlayRightTrim);

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

        LocationLayer front = new LocationLayer(base.getWidth(), base.getHeight(), PX, PX, map);
        front.setVisible(true);
        front.setOpacity(1.0f);
        front.setName("overlay-trim-front");

        // fill in front trim
        for (int x = 0; x < trim.getWidth(); x++) {
            for (int y = 0; y < trim.getHeight(); y++) {
                if (!base.isWall(x, y) && base.isWall(x, y - 1)) {
                    addCell(front, walls.getTile(WallTile.FrontMiddleTrim), x, y);
                }
            }
        }

        // fill in corners
        walls.getTile(WallTile.TopLeftCorner).setOffsetX(Settings.PX / 2);
        for (int x = 0; x < trim.getWidth(); x++) {
            for (int y = 0; y < trim.getHeight(); y++) {
                if (!front.hasCell(x, y) && trim.hasCell(x, y - 1)) {
                    if (matchesTile(front, x + 1, y, walls.getTile(WallTile.FrontMiddleTrim))) {
                        // top left corner
                        addCell(front, walls.getTile(WallTile.TopLeftCorner), x, y);
                    } else if (matchesTile(front, x - 1, y, walls.getTile(WallTile.FrontMiddleTrim))) {
                        // top right corner
                        addCell(front, walls.getTile(WallTile.TopRightCorner), x, y);
                    }
                }
            }
        }

        LocationLayer left = new LocationLayer(base.getWidth(), base.getHeight(), PX, PX, map);
        left.setVisible(true);
        left.setOpacity(1.0f);
        left.setName("overlay-trim-left");

        LocationLayer right = new LocationLayer(base.getWidth(), base.getHeight(), PX, PX, map);
        right.setVisible(true);
        right.setOpacity(1.0f);
        right.setName("overlay-trim-right");

        for (int x = 0; x < trim.getWidth(); x++) {
            for (int y = 0; y < trim.getHeight(); y++) {
                if (!base.isWall(x, y) && base.isWall(x, y - 1)
                        && (trim.hasCell(x, y) || overlay.hasCell(x, y))) {
                    if (!front.hasCell(x - 1, y) && !trim.hasCell(x - 1, y)) {
                        addCell(left, walls.getTile(WallTile.FrontLeftTrim), x, y);
                    }
                    if (!front.hasCell(x + 1, y) && !trim.hasCell(x + 1, y)) {
                        addCell(right, walls.getTile(WallTile.FrontRightTrim), x, y);
                    }
                }
            }
        }

        LocationLayer lcorner = new LocationLayer(base.getWidth(), base.getHeight(), PX, PX, map);
        lcorner.setVisible(true);
        lcorner.setOpacity(1.0f);
        lcorner.setName("overlay-trim-left");

        LocationLayer rcorner = new LocationLayer(base.getWidth(), base.getHeight(), PX, PX, map);
        rcorner.setVisible(true);
        rcorner.setOpacity(1.0f);
        rcorner.setName("overlay-trim-right");

        // bottom left
        TiledMapTile leftCorner = walls.getTile(WallTile.LeftCorner);

        // bottom right
        TiledMapTile rightCorner = walls.getTile(WallTile.RightCorner);

        // required offsets
        leftCorner.setOffsetX(Settings.PX / 2);
        leftCorner.setOffsetY(Settings.PX / 2);
        rightCorner.setOffsetY(Settings.PX / 2);

        // fill in corners
        for (int x = 0; x < roof.getWidth(); x++) {
            for (int y = 0; y < roof.getHeight(); y++) {
                if (overlay.isFilled(x + 1, y)
                        && (trim.isFilled(x, y + 1) || overlay.isFilled(x, y + 1))) {
                    // case I: overlay to the right, wall at current position
                    // add a left corner here
                    addCell(lcorner, leftCorner, x, y);
                }
            }
        }

        for (int x = 0; x < roof.getWidth(); x++) {
            for (int y = 0; y < roof.getHeight(); y++) {
                if (overlay.isFilled(x - 1, y)
                        && (trim.isFilled(x, y + 1) || overlay.isFilled(x, y + 1))) {
                    // case II: overlay to the left, wall at current position
                    // add a right corner here
                    addCell(rcorner, rightCorner, x, y);
                }
            }
        }

        return ImmutableList.of(layer1, front, left, right, lcorner, rcorner);
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
            RoomGenerator generator, LocationMap map, ConnectedRoomManager rooms, int level,
            List<Encounter> encounterList) {
        Set<Encounter> encounters = new LinkedHashSet<>(encounterList);
        List<LocationLayer> layers = new ArrayList<LocationLayer>();
        for (ControlRoom controlRoom : generator.getEncounterRooms()) {
            // further restrict bounds to prevent spawning at wall level
            Rectangle bounds = new Rectangle(controlRoom.getRestrictedBounds());
            bounds.height -= 1;

            ConnectedRoom connected = rooms.getConnected(controlRoom);
            List<NaturalVector2> freeSpaces = getFreeSpaces(collision, bounds);
            freeSpaces.retainAll(connected.getPoints());

            // generate the player layer
            if (isSpawnRoom(controlRoom.getControlPoint())) {
                LocationLayer playerLayer = new LocationLayer(base.getWidth(), base.getHeight(),
                        PX, PX, map);
                playerLayer.setVisible(false);
                playerLayer.setOpacity(1.0f);
                playerLayer.setName("player");

                Collections.shuffle(freeSpaces, rand);
                Iterator<NaturalVector2> it = freeSpaces.iterator();
                if (!it.hasNext()) {
                    // TODO: this should never happen, but just in case we
                    // should regenerate the
                    // whole map
                }

                NaturalVector2 position = it.next();
                addCell(playerLayer, collider, position.x, position.y);
                it.remove();
                layers.add(playerLayer);
            }

            // generate encounter layers
            Optional<Encounter> encounter = controlRoom.chooseEncounter(level, encounters, rooms);
            if (encounter.isPresent()) {
                createLayer(bounds, encounter.get(), freeSpaces, rooms, base, collision, map,
                        layers);

                if (encounter.get().getUnique()) {
                    // remove the encounter from the list of possibilities
                    encounters.remove(encounter.get());
                }
            }
        }

        return layers;
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

    private boolean isSpawnRoom(ControlPoint cp) {
        return cp.getOrigin();
    }

    private void createLayer(Rectangle bounds, Encounter encounter,
            List<NaturalVector2> freeSpaces, ConnectedRoomManager rooms, LocationLayer base,
            LocationLayer collision, LocationMap map, List<LocationLayer> layers) {
        Set<NaturalVector2> used = new HashSet<>();
        EncounterDescription encounterDescription = new EncounterDescription();

        // enemy placement can be non-deterministic between loads
        Collections.shuffle(freeSpaces);
        Iterator<NaturalVector2> it = freeSpaces.iterator();
        for (ActorScenario scenario : encounter.getActorParams().getActorScenarioList()) {
            int count = getCount(encounter, scenario);
            for (int i = 0; i < count; i++) {
                NaturalVector2 position = it.hasNext() ? it.next() : getPoint(bounds, base, used);
                ConnectedRoom room = rooms.getRoom(position.x, position.y);
                AgentDescription agentDescription = new AgentDescription(scenario, position, room);
                encounterDescription.addAgent(agentDescription);
                used.add(position);
            }
        }
        map.addEncounter(encounterDescription);
    }

    public int getCount(Encounter encounter, ActorScenario scenario) {
        // the greater the delta, the greater the chance of getting a higher
        // count
        int delta = encounter.getMinLevel() - floor;
        float target = rand.nextFloat() * Heuristics.sigmoid(delta);

        // fit the target between the min and the max
        float count = target * (scenario.getMax() - scenario.getMin()) + scenario.getMin();
        return Math.round(count);
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

    private void addWalls(LocationLayer layer, LocationLayer left, LocationLayer right,
            CellType[][] typeMap) {
        for (int x = 0; x < layer.getWidth(); x++) {
            for (int y = 0; y < layer.getHeight(); y++) {
                Cell cell = layer.getCell(x, y);
                if (cell != null && cell.getTile() == ground) {
                    // check for empty space above
                    if (y + 2 < layer.getHeight() && layer.getCell(x, y + 2) == null) {
                        addCell(layer, walls.getTile(WallTile.MidWallBottom), x, y + 0);
                        addCell(layer, walls.getTile(WallTile.MidWallTop), x, y + 1);
                    }
                }
            }
        }

        // add bookend walls
        for (int x = 0; x < layer.getWidth(); x++) {
            for (int y = 0; y < layer.getHeight(); y++) {
                // check ground below, wall here
                if (typeMap[x][y] == CellType.Floor && layer.isWall(x, y) && layer.isWall(x, y + 1)) {
                    // check for ground left or right
                    if (!layer.isWall(x - 1, y)) {
                        // no wall to the left
                        addCell(left, walls.getTile(WallTile.LeftWallBottom), x, y + 0);
                        addCell(left, walls.getTile(WallTile.LeftWallTop), x, y + 1);
                    }
                    if (!layer.isWall(x + 1, y)) {
                        // no wall to the right
                        addCell(right, walls.getTile(WallTile.RightWallBottom), x, y + 0);
                        addCell(right, walls.getTile(WallTile.RightWallTop), x, y + 1);
                    }
                }
            }
        }
    }

    public NaturalVector2 getPoint(Rectangle rect, LocationLayer base, Set<NaturalVector2> used) {
        int left = (int) rect.x * SCALE;
        int right = (int) (left + rect.width * SCALE);
        int top = (int) rect.y * SCALE;
        int bottom = (int) (top + rect.height * SCALE);
        NaturalVector2 seed = NaturalVector2.of(randomNumber(left + 1, right - 2),
                randomNumber(top + 1, bottom - 2));
        return getPoint(rect, base, used, seed);
    }

    public NaturalVector2 getPoint(Rectangle rect, LocationLayer base, Set<NaturalVector2> used,
            NaturalVector2 seed) {
        Set<NaturalVector2> visited = new LinkedHashSet<NaturalVector2>();
        LinkedList<NaturalVector2> queue = new LinkedList<NaturalVector2>();

        queue.add(seed);
        visited.add(seed);
        while (!queue.isEmpty()) {
            NaturalVector2 point = queue.remove();
            if (base.isGround(point.x, point.y) && !used.contains(point)) {
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
        TextureRegion normal = left.hasNormal() && right.hasNormal() ? merge(
                left.getNormalRegion(), right.getNormalRegion()) : null;
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

    public static <T> void save(T[][] grid, String filename) {
        int width = grid.length;
        int height = grid[0].length;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // every element has a unique color in the grid
        Random rand = new Random();
        Map<T, Color> colors = new HashMap<T, Color>();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                T elem = grid[x][height - y - 1]; // images have (0, 0) in
                                                  // upper-left
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
