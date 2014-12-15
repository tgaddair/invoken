package com.eldritch.invoken.encounter.proc;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.imageio.ImageIO;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.type.Player;
import com.eldritch.invoken.encounter.Activator;
import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.encounter.NaturalVector2;
import com.eldritch.invoken.encounter.layer.EncounterLayer;
import com.eldritch.invoken.encounter.layer.LocationCell;
import com.eldritch.invoken.encounter.layer.LocationLayer;
import com.eldritch.invoken.encounter.layer.LocationLayer.CollisionLayer;
import com.eldritch.invoken.encounter.layer.LocationMap;
import com.eldritch.invoken.encounter.proc.BspGenerator.CellType;
import com.eldritch.invoken.encounter.proc.RoomGenerator.RoomType;
import com.eldritch.invoken.gfx.Light;
import com.eldritch.invoken.proto.Locations.Biome;
import com.eldritch.invoken.proto.Locations.Encounter;
import com.eldritch.invoken.proto.Locations.Encounter.ActorParams.ActorScenario;
import com.eldritch.invoken.proto.Locations.Room;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class LocationGenerator {
	// string constants required for all biome types
	private static final String FLOOR = "/floor";
	private static final String ROOF = "/roof";
	private static final String MID_WALL_TOP = "/mid-wall-top";
	private static final String MID_WALL_CENTER = "/mid-wall-center";
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
	
    private static final int PX = Location.PX;
    private static final int SCALE = 1;
    private static final int MAX_LEAF_SIZE = 35;
    private final Random rand = new Random();

    private final String biome;
    private final TextureAtlas atlas;
    
    private final TiledMapTile ground;
    private final TiledMapTile midWallTop;
    //private final TiledMapTile midWallCenter;
    private final TiledMapTile midWallBottom;
    
    private final TiledMapTile leftTrim;
    private final TiledMapTile rightTrim;
    private final TiledMapTile narrowWall;
    private final TiledMapTile narrowTop;
    private final TiledMapTile collider;
    
    public LocationGenerator(Biome biomeType) {
    	this.biome = biomeType.name().toLowerCase();
        atlas = new TextureAtlas(Gdx.files.internal("image-atlases/pages.atlas"));

        AtlasRegion region = atlas.findRegion(biome + FLOOR);
        ground = new StaticTiledMapTile(region);
        
        midWallTop = new StaticTiledMapTile(atlas.findRegion(biome + MID_WALL_TOP));
        //midWallCenter = new StaticTiledMapTile(atlas.findRegion(biome + MID_WALL_CENTER));
        midWallBottom = new StaticTiledMapTile(atlas.findRegion(biome + MID_WALL_BOTTOM));
        
        leftTrim = new StaticTiledMapTile(atlas.findRegion(biome + LEFT_TRIM));
        rightTrim = new StaticTiledMapTile(atlas.findRegion(biome + RIGHT_TRIM));
        narrowWall = merge(rightTrim.getTextureRegion(), leftTrim.getTextureRegion());
        narrowTop = merge(
        		atlas.findRegion(biome + TOP_LEFT_TRIM),
        		atlas.findRegion(biome + TOP_RIGHT_TRIM));
        collider = new StaticTiledMapTile(atlas.findRegion(COLLISION));
    }

    public Location generate(com.eldritch.invoken.proto.Locations.Location proto, Player player) {
        int width = Location.MAX_WIDTH;
        int height = Location.MAX_HEIGHT;
        LocationMap map = getBaseMap(width, height);
//        List<Leaf> leafs = createLeaves(width / SCALE, height / SCALE);
//        List<Room> rooms = createRooms(leafs);
        
        BspGenerator bsp = new BspGenerator(width / SCALE, height / SCALE);
        bsp.generateSegments();
        bsp.save();
        CellType[][] typeMap = bsp.getMap();

        // create layers
        InvokenGame.log("Creating Base");
        LocationLayer base = createBaseLayer(typeMap, width, height, map);
        map.getLayers().add(base);

        InvokenGame.log("Creating Trim");
        LocationLayer trim = createTrimLayer(base, map);
        map.getLayers().add(trim);

        InvokenGame.log("Creating Overlay");
        LocationLayer overlay = createOverlayLayer(base, map);
        map.getLayers().add(overlay);
        
        InvokenGame.log("Creating Overlay Trim");
        LocationLayer overlayTrim = createOverlayTrimLayer(base, overlay, map);
        map.getLayers().add(overlayTrim);
        
        InvokenGame.log("Creating Collision");
        CollisionLayer collision = createCollisionLayer(base, map);
        map.getLayers().add(collision);
        
        InvokenGame.log("Creating Roof");
        TiledMapTile roof = new StaticTiledMapTile(atlas.findRegion(biome + ROOF));
        for (int i = 0; i < typeMap.length; i++) {
            for (int j = 0; j < typeMap[i].length; j++) {
                if (typeMap[i][j] != CellType.Floor) {
                    addTile(i, j, base, roof);
                }
            }
        }
        
        InvokenGame.log("Adding Rooms");
        RoomGenerator roomGenerator = new RoomGenerator(map);
        roomGenerator.generate(bsp.getRooms(), proto.getEncounterList());
        
        InvokenGame.log("Creating Spawn Layers");
        for (LocationLayer layer : createSpawnLayers(base, collision, bsp.getRooms(),
                proto.getEncounterList(), map)) {
            map.getLayers().add(layer);
        }

        // add furniture
        InvokenGame.log("Adding Furniture");
        List<Activator> activators = new ArrayList<Activator>();
        IcarianFurnitureGenerator furnitureGenerator = new IcarianFurnitureGenerator(atlas, ground);

        // doors
        InvokenGame.log("Adding Doors");
        furnitureGenerator.createDoors(base, trim, overlay, overlayTrim, collision, activators);

        // lights
        InvokenGame.log("Adding Lights");
        List<Light> lights = new ArrayList<Light>();
        furnitureGenerator.addLights(trim, base, lights, midWallTop);

        // clutter
        InvokenGame.log("Adding Clutter");
//        map.getLayers().add(furnitureGenerator.generateClutter(base, map));

        Location location = new Location(proto, player, map);
        location.addLights(lights);
        location.addActivators(activators);
        
        // debug
        saveLayer(base);

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

    private LocationLayer createBaseLayer(CellType[][] typeMap, int width, int height, LocationMap map) {
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

//        for (Leaf leaf : leafs) {
//            if (leaf.room != null) {
//                addRoom(leaf.room, layer, ground);
//            }
//            for (Rectangle hall : leaf.halls) {
//                addRoom(hall, layer, ground);
//            }
//        }

        // add walls
        addWalls(layer);
        
        InvokenGame.log("done");

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
            	if (base.getCell(x, y) == null) {
            		boolean leftGround = x - 1 >= 0 && base.getCell(x - 1, y) != null;
            		boolean rightGround = x + 1 < base.getWidth() && base.getCell(x + 1, y) != null;
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

        TiledMapTile leftCorner = new StaticTiledMapTile(atlas.findRegion(biome + LEFT_CORNER));
        TiledMapTile rightCorner = new StaticTiledMapTile(
                atlas.findRegion(biome + RIGHT_CORNER));
        
        // required offsets
        leftCorner.setOffsetX(Location.PX / 2);
        leftCorner.setOffsetY(Location.PX / 2);
        rightCorner.setOffsetY(Location.PX / 2);

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
                atlas.findRegion(biome + OVERLAY_BELOW_TRIM));
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

    private LocationLayer createOverlayTrimLayer(LocationLayer base, LocationLayer overlay,
            LocationMap map) {
        LocationLayer layer = new LocationLayer(base.getWidth(), base.getHeight(), PX, PX, map);
        layer.setVisible(true);
        layer.setOpacity(1.0f);
        layer.setName("overlay-trim");

        TiledMapTile overlayLeftTrim = new StaticTiledMapTile(
                atlas.findRegion(biome + OVERLAY_LEFT_TRIM));
        TiledMapTile overlayRightTrim = new StaticTiledMapTile(
                atlas.findRegion(biome + OVERLAY_RIGHT_TRIM));

        // fill in sides
        for (int x = 0; x < overlay.getWidth(); x++) {
            for (int y = 0; y < overlay.getHeight(); y++) {
                Cell cell = overlay.getCell(x, y);
                if (cell != null) {
                	boolean lGround = base.isGround(x - 1, y) && overlay.getCell(x - 1, y) == null;
                	boolean rGround = base.isGround(x + 1, y) && overlay.getCell(x + 1, y) == null;
                    if (lGround) {
                    	if (rGround) {
                    		// narrow top
                    		addCell(layer, narrowTop, x, y);
                    	} else {
                    		// left space is ground
                    		addCell(layer, overlayRightTrim, x, y);
                    	}
                    } else if (rGround) {
                        // right space is ground
                        addCell(layer, overlayLeftTrim, x, y);
                    }
                }
            }
        }

        return layer;
    }

    private CollisionLayer createCollisionLayer(LocationLayer base, LocationMap map) {
        CollisionLayer layer = new CollisionLayer(base.getWidth(), base.getHeight(), PX, PX, map,
                collider);
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

    private List<GeneratedRoom> createRooms(List<Leaf> leafs) {
        List<GeneratedRoom> generatedRooms = new ArrayList<GeneratedRoom>();

        // create rooms
        Map<Leaf, GeneratedRoom> leafToRoom = new HashMap<Leaf, GeneratedRoom>();
        for (Leaf leaf : leafs) {
            if (leaf.room != null) {
                GeneratedRoom generatedRoom = new GeneratedRoom(leaf);
                generatedRooms.add(generatedRoom);
                leafToRoom.put(leaf, generatedRoom);
            }
        }

        // define neighbor rooms for later graph traversal
        for (GeneratedRoom generatedRoom : generatedRooms) {
            Leaf leaf = generatedRoom.getLeaf();
            if (leaf.leftChild != null && leafToRoom.containsKey(leaf.leftChild)) {
                GeneratedRoom other = leafToRoom.get(leaf.leftChild);
                generatedRoom.addAdjacentRoom(other);
                other.addAdjacentRoom(generatedRoom);
            }
            if (leaf.rightChild != null && leafToRoom.containsKey(leaf.rightChild)) {
                GeneratedRoom other = leafToRoom.get(leaf.rightChild);
                generatedRoom.addAdjacentRoom(other);
                other.addAdjacentRoom(generatedRoom);
            }
        }

        return generatedRooms;
    }

    private List<LocationLayer> createSpawnLayers(LocationLayer base, LocationLayer collision,
            List<Rectangle> rooms, List<Encounter> encounters, LocationMap map) {
    	List<Encounter> availableEncounters = new ArrayList<Encounter>(encounters);
        List<LocationLayer> layers = new ArrayList<LocationLayer>();
        LocationLayer playerLayer = null;
        for (Rectangle room : rooms) {
        	double total = getTotalWeight(encounters);
            if (playerLayer == null) {
                playerLayer = new LocationLayer(base.getWidth(), base.getHeight(), PX, PX, map);
                playerLayer.setVisible(false);
                playerLayer.setOpacity(1.0f);
                playerLayer.setName("player");

                NaturalVector2 position = getPoint(room, base, playerLayer);
                addCell(playerLayer, collider, position.x, position.y);

                layers.add(playerLayer);
            } else {
                Encounter encounter = popEncounter(room, availableEncounters, total);
                if (encounter != null) {
                    LocationLayer layer = createLayer(encounter, room, base, collision, map);
                    layers.add(layer);
                }
            }
        }

        return layers;
    }

    private Encounter popEncounter(Rectangle room, List<Encounter> encounters, double total) {
        double target = Math.random() * total;
        double sum = 0.0;
        Iterator<Encounter> it = encounters.iterator();
        while (it.hasNext()) {
        	Encounter encounter = it.next();
        	if (!compatible(encounter, room)) {
        	    // basic check to make sure the dimensions fit together
        	    continue;
        	}
        	
            if (encounter.getType() == Encounter.Type.ACTOR) {
                sum += encounter.getWeight();
                if (sum >= target) {
                    if (encounter.getUnique()) {
                		it.remove();
                	}
                    
                    return encounter;
                }
            }
        }
        return null;
    }
    
    private final LoadingCache<String, Room> availableRooms = CacheBuilder.newBuilder()
            .build(new CacheLoader<String, Room>() {
              public Room load(String roomId) {
                  return InvokenGame.ROOM_READER.readAsset(roomId);
              }
            });
    
    private Room lookupRoom(String roomId) {
        try {
            return availableRooms.get(roomId);
        } catch (Exception ex) {
            InvokenGame.error("Failed to load room: " + roomId, ex);
            return null;
        }
    }
    
    // TODO: this is redundant, as we'll need to do this check again to actually fetch a compatible
    // room for the encounter; better to just compute the actual room here and store it off
    private boolean compatible(Encounter encounter, Rectangle bounds) {
        // if the room list is empty, then the encounter can go in any room
        if (encounter.getRoomIdList().isEmpty()) {
            return true;
        }
        
        for (String roomId : encounter.getRoomIdList()) {
            Room room = lookupRoom(roomId);
            RoomType type = RoomGenerator.get(room.getSize());
            if (type.fitsBounds(bounds)) {
                return true;
            }
        }
        
        // no match, but there were room restrictions in place
        return false;
    }
    
    private LocationLayer createLayer(Encounter encounter, Rectangle room, LocationLayer base,
            LocationLayer collision, LocationMap map) {
        LocationLayer layer = new EncounterLayer(encounter, base.getWidth(),
                base.getHeight(), PX, PX, map);
        layer.setVisible(false);
        layer.setOpacity(1.0f);
        layer.setName("encounter-" + room.getX() + "-" + room.getY());

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

    public static double getTotalWeight(List<Encounter> encounters) {
        double total = 0.0;
        for (Encounter encounter : encounters) {
            if (encounter.getType() == Encounter.Type.ACTOR) {
                total += encounter.getWeight();
            }
        }
        return total;
    }

    private void addWalls(LocationLayer layer) {
        for (int x = 0; x < layer.getWidth(); x++) {
            for (int y = 0; y < layer.getHeight(); y++) {
                Cell cell = layer.getCell(x, y);
                if (cell != null && cell.getTile() == ground) {
                    // check for empty space above
                    if (y + 2 < layer.getHeight() && layer.getCell(x, y + 2) == null) {
                        addCell(layer, midWallBottom, x, y + 0);
                        //addCell(layer, midWallCenter, x, y + 1);
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
                addTile(i, j, layer, tile);
            }
        }
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
                    if (l.width > MAX_LEAF_SIZE || l.height > MAX_LEAF_SIZE || Math.random() < 0.5) {
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
    
    private static TiledMapTile merge(TextureRegion left, TextureRegion right) {
    	FrameBuffer buffer = new FrameBuffer(Format.RGB888, Location.PX, Location.PX, false);
    	TextureRegion region = new TextureRegion(buffer.getColorBufferTexture());
        region.flip(false, true);
        
        // extract the part of each region we care about
        int size = Location.PX / 2;
        TextureRegion leftPart = new TextureRegion(left, 0, 0, size, Location.PX);
        TextureRegion rightPart = new TextureRegion(right, size, 0, size, Location.PX);
        
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
        
        return new StaticTiledMapTile(region);
    }

    private void saveLayer(LocationLayer layer) {
        BufferedImage image = new BufferedImage(layer.getWidth(), layer.getHeight(),
                BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < layer.getWidth(); x++) {
            for (int y = 0; y < layer.getHeight(); y++) {
                Cell cell = layer.getCell(x, y);
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
}
