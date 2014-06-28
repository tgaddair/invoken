package com.eldritch.invoken.encounter.proc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Player;
import com.eldritch.invoken.encounter.Activator;
import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.encounter.NaturalVector2;
import com.eldritch.invoken.encounter.layer.EncounterLayer;
import com.eldritch.invoken.encounter.layer.LocationCell;
import com.eldritch.invoken.encounter.layer.LocationLayer;
import com.eldritch.invoken.encounter.layer.LocationLayer.CollisionLayer;
import com.eldritch.invoken.encounter.layer.LocationMap;
import com.eldritch.invoken.gfx.Light;
import com.eldritch.invoken.gfx.Light.StaticLight;
import com.eldritch.invoken.proto.Locations.Encounter;
import com.eldritch.invoken.proto.Locations.Encounter.ActorParams.ActorScenario;

public class LocationGenerator {
    private static final int PX = Location.PX;
    private static final int SCALE = 2;
    private static final int MAX_LEAF_SIZE = 35;
    private final Random rand = new Random();
    private final TextureAtlas atlas;
    private final TiledMapTile ground;
    private final TiledMapTile midWallCenter;
    private final TiledMapTile leftTrim;
    private final TiledMapTile rightTrim;
    private final TiledMapTile collider;

    public LocationGenerator() {
        atlas = new TextureAtlas(Gdx.files.internal("image-atlases/pages.atlas"));

        AtlasRegion region = atlas.findRegion("test-biome/floor4");
        ground = new StaticTiledMapTile(region);
        midWallCenter = new StaticTiledMapTile(atlas.findRegion("test-biome/mid-wall-center"));
        leftTrim = new StaticTiledMapTile(atlas.findRegion("test-biome/left-trim"));
        rightTrim = new StaticTiledMapTile(atlas.findRegion("test-biome/right-trim"));
        collider = new StaticTiledMapTile(atlas.findRegion("markers/collision"));
    }

    public Location generate(com.eldritch.invoken.proto.Locations.Location proto, Player player) {
        int width = Location.MAX_WIDTH;
        int height = Location.MAX_HEIGHT;
        LocationMap map = getBaseMap(width, height);
        List<Leaf> leafs = createLeaves(width / SCALE, height / SCALE);

        List<Light> lights = new ArrayList<Light>();

        // create layers
        LocationLayer base = createBaseLayer(leafs, width, height, map);
        map.getLayers().add(base);

        LocationLayer trim = createTrimLayer(base, map);
        map.getLayers().add(trim);
        
        LocationLayer overlay = createOverlayLayer(base, map);
        map.getLayers().add(overlay);
        LocationLayer overlayTrim = createOverlayTrimLayer(base, overlay, map);
        map.getLayers().add(overlayTrim);
        
        CollisionLayer collision = createCollisionLayer(base, map);
        map.getLayers().add(collision);
        for (LocationLayer layer : createSpawnLayers(base, leafs, proto.getEncounterList(), map)) {
            map.getLayers().add(layer);
        }

        // add furniture
        List<Activator> activators = new ArrayList<Activator>();
        IcarianFurnitureGenerator furnitureGenerator = new IcarianFurnitureGenerator(atlas, ground);
        
        // doors
        furnitureGenerator.createDoors(base, trim, overlay, overlayTrim, collision, activators);
        
        // lights
        furnitureGenerator.addLights(trim, base, lights, midWallCenter);
        
        // clutter
        map.getLayers().add(furnitureGenerator.generateClutter(base, map));
        
        Location location = new Location(proto, player, map);
        location.addLights(lights);
        location.addActivators(activators);

        return location;
    }

    private LocationMap getBaseMap(int width, int height) {
        LocationMap map = new LocationMap(ground);
        MapProperties mapProperties = map.getProperties();
        mapProperties.put("width", width);
        mapProperties.put("height", height);
        mapProperties.put("tilewidth", PX);
        mapProperties.put("tileheight", PX);
        return map;
    }

    private LocationLayer createBaseLayer(List<Leaf> leafs, int width, int height, LocationMap map) {
        LocationLayer layer = new LocationLayer(width, height, PX, PX, map);
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

    private LocationLayer createTrimLayer(LocationLayer base, LocationMap map) {
        LocationLayer layer = new LocationLayer(base.getWidth(), base.getHeight(), PX, PX, map);
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

    private LocationLayer createOverlayLayer(LocationLayer base, LocationMap map) {
        LocationLayer layer = new LocationLayer(base.getWidth(), base.getHeight(), PX, PX, map);
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

    private LocationLayer createOverlayTrimLayer(LocationLayer base,
            LocationLayer overlay, LocationMap map) {
        LocationLayer layer = new LocationLayer(base.getWidth(), base.getHeight(), PX, PX, map);
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
                    if (base.isGround(x - 1, y) && overlay.getCell(x - 1, y) == null) {
                        // left space is ground
                        addCell(layer, overlayRightTrim, x, y);
                    }
                    if (base.isGround(x + 1, y) && overlay.getCell(x + 1, y) == null) {
                        // right space is ground
                        addCell(layer, overlayLeftTrim, x, y);
                    }
                }
            }
        }

        return layer;
    }

    private CollisionLayer createCollisionLayer(LocationLayer base, LocationMap map) {
        CollisionLayer layer = new CollisionLayer(base.getWidth(), base.getHeight(), PX, PX, map, collider);
        layer.setVisible(false);
        layer.setOpacity(1.0f);
        layer.setName("collision");

        for (int x = 0; x < base.getWidth(); x++) {
            for (int y = 0; y < base.getHeight(); y++) {
                Cell cell = base.getCell(x, y);
                if (cell == null || cell.getTile() != ground) {
                    // non-empty, non-ground space
                    addCell(layer, collider, x, y);
                }
            }
        }

        return layer;
    }

    private List<LocationLayer> createSpawnLayers(LocationLayer base, List<Leaf> leafs,
            List<Encounter> encounters, LocationMap map) {
        List<LocationLayer> layers = new ArrayList<LocationLayer>();
        LocationLayer playerLayer = null;
        double total = getTotalWeight(encounters);
        for (Leaf leaf : leafs) {
            if (leaf.room != null) {
                if (playerLayer == null) {
                    playerLayer = new LocationLayer(base.getWidth(), base.getHeight(),
                            PX, PX, map);
                    playerLayer.setVisible(false);
                    playerLayer.setOpacity(1.0f);
                    playerLayer.setName("player");
                    
                    NaturalVector2 position = getPoint(leaf.room, base, playerLayer);
                    addCell(playerLayer, collider, position.x, position.y);
                    
                    layers.add(playerLayer);
                } else {
                    LocationLayer layer = addEncounter(leaf, encounters, base, total, map);
                    if (layer != null) {
                        layers.add(layer);
                    }
                }
            }
        }

        return layers;
    }

    private LocationLayer addEncounter(Leaf leaf, List<Encounter> encounters,
            LocationLayer base, double total, LocationMap map) {
        double target = Math.random() * total;
        double sum = 0.0;
        for (Encounter encounter : encounters) {
            if (encounter.getType() == Encounter.Type.ACTOR) {
                sum += encounter.getWeight();
                if (sum >= target) {
                    LocationLayer layer = new EncounterLayer(encounter, base.getWidth(),
                            base.getHeight(), PX, PX, map);
                    layer.setVisible(false);
                    layer.setOpacity(1.0f);
                    layer.setName("encounter-" + leaf.x + "-" + leaf.y);

                    for (ActorScenario scenario : encounter.getActorParams().getActorScenarioList()) {
                        NaturalVector2 position = getPoint(leaf.room, base, layer);
                        addCell(layer, collider, position.x, position.y);
                    }
                    return layer;
                }
            }
        }
        return null;
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

    private void addWalls(LocationLayer layer) {
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

    public NaturalVector2 getPoint(Rectangle rect, LocationLayer base, LocationLayer layer) {
        int left = (int) rect.x * SCALE;
        int right = (int) (left + rect.width * SCALE);
        int top = (int) rect.y * SCALE;
        int bottom = (int) (top + rect.height * SCALE);
        NaturalVector2 seed = NaturalVector2.of(randomNumber(left + 1, right - 2), randomNumber(
                top + 1, bottom - 2));
        return getPoint(rect, base, layer, seed);
    }

    public NaturalVector2 getPoint(Rectangle rect, LocationLayer base, LocationLayer layer,
            NaturalVector2 seed) {
        Set<NaturalVector2> visited = new HashSet<NaturalVector2>();
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

    private void addRoom(Rectangle room, LocationLayer layer, TiledMapTile tile) {
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

    private static Cell addCell(LocationLayer layer, TiledMapTile tile, int x, int y) {
        Cell cell = new LocationCell(NaturalVector2.of(x, y), layer);
        cell.setTile(tile);
        layer.setCell(x, y, cell);
        return cell;
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
