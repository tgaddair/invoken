package com.eldritch.invoken.encounter.proc;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.math.Rectangle;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.encounter.NaturalVector2;
import com.eldritch.invoken.encounter.layer.LocationLayer;
import com.eldritch.invoken.encounter.layer.LocationMap;
import com.eldritch.invoken.proto.Locations.Encounter;
import com.eldritch.invoken.proto.Locations.Room;
import com.eldritch.invoken.proto.Locations.Room.Furniture;
import com.eldritch.invoken.util.FurnitureLoader;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class RoomGenerator {
    // threshold of furniture to open ground in room, past which we need to stop adding furniture
    public static final double MAX_FURNITURE = 0.5;
    
    enum RoomType {
        SMALL(0, 49), MEDIUM(36, 100), LARGE(81, Integer.MAX_VALUE);

        private final int min;
        private final int max;

        private RoomType(int min, int max) {
            this.min = min;
            this.max = max;
        }
        
        public boolean fitsBounds(Rectangle bounds) {
            float area = bounds.area();
            return area >= min && area <= max;
        }
    }

    private static final EnumMap<Room.Size, RoomType> sizeToType = new EnumMap<Room.Size, RoomType>(
            Room.Size.class);
    
    static {
        sizeToType.put(Room.Size.SMALL, RoomType.SMALL);
        sizeToType.put(Room.Size.MEDIUM, RoomType.MEDIUM);
        sizeToType.put(Room.Size.LARGE, RoomType.LARGE);
    }
    
    private final LoadingCache<String, Room> availableRooms = CacheBuilder.newBuilder()
            .build(new CacheLoader<String, Room>() {
              public Room load(String roomId) {
                  return InvokenGame.ROOM_READER.readAsset(roomId);
              }
            });
    
    private final Map<String, List<Rectangle>> roomsById = new HashMap<String, List<Rectangle>>();
    private final LocationMap map;
    
    public RoomGenerator(LocationMap map) {
        this.map = map;
    }
    
    public void generate(List<Rectangle> bounds, List<Encounter> encounters) {
        List<Encounter> available = new ArrayList<Encounter>(encounters);
        removeRoomless(available);
        LocationGenerator.sortByWeight(available);
        
        List<Rectangle> unplaced = new ArrayList<Rectangle>(bounds);
        while (!unplaced.isEmpty() && !available.isEmpty()) {
            double total = LocationGenerator.getTotalWeight(encounters);
            process(available, unplaced, total);
        }
    }
    
    private void process(List<Encounter> encounters, List<Rectangle> bounds, double total) {
        double target = Math.random() * total;
        double sum = 0.0;
        Iterator<Encounter> it = encounters.iterator();
        while (it.hasNext()) {
            Encounter encounter = it.next();
            sum += encounter.getWeight();
            if (sum >= target || encounter.getUnique()) {
                // place this encounter
                if (!place(encounter, bounds) || encounter.getUnique()) {
                    // failed to place the encounter or it's unique, so we don't wish to reuse it
                    it.remove();
                }
            }
        }
    }
    
    private boolean place(Encounter encounter, List<Rectangle> bounds) {
        Iterator<Rectangle> it = bounds.iterator();
        while (it.hasNext()) {
            Rectangle rect = it.next();
            for (String roomId : encounter.getRoomIdList()) {
                Room room = lookupRoom(roomId);
                RoomType type = get(room.getSize());
                if (type.fitsBounds(rect)) {
                    // room type fits, so do the placement
                    place(rect, room);
                    put(roomId, rect);
                    it.remove();
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private void place(Rectangle rect, Room room) {
        double area = rect.area();
        int coveredTiles = 0;  // running count of covered tiles
        for (Furniture furniture : room.getFurnitureList()) {
            TiledMap roomMap = FurnitureLoader.load(furniture);
            
            // calculate the percentage of furniture coverage to ground tiles adding this piece of
            // furniture would cost us
            int cost = getCost(roomMap);
            double coverage = (coveredTiles + cost) / area;
            
            // find a suitable place in room that satisfies the constraints
            if (coverage < MAX_FURNITURE) {
                NaturalVector2 offset = findOffset(roomMap, rect);
                if (offset != null) {
                    // found a place to put the furniture, so merge it into the map
                    map.merge(roomMap, offset);
                }
            }
        }
    }
    
    private NaturalVector2 findOffset(TiledMap furniture, Rectangle rect) {
        Map<String, LocationLayer> presentLayers = map.getLayerMap();
        for (int x = 0; x < map.getWidth(); x++) {
            for (int y = 0; y < map.getHeight(); y++) {
                if (compatible(presentLayers, furniture, x, y)) {
                    return NaturalVector2.of(x, y);
                }
            }
        }
        return null;
    }
    
    private boolean compatible(Map<String, LocationLayer> presentLayers, TiledMap furniture,
            int x, int y) {
        for (MapLayer mapLayer : furniture.getLayers()) {
            TiledMapTileLayer layer = (TiledMapTileLayer) mapLayer;
            if (layer.getName().equals("constraints")) {
                // special case
            } else {
                LocationLayer existing = presentLayers.get(mapLayer.getName());
                if (existing == null) {
                    // no equivalent layer to be in conflict with
                    continue;
                }
                
                for (int i = 0; i < layer.getWidth(); i++) {
                    for (int j = 0; j < layer.getHeight(); j++) {
                        if (!inBounds(x + i, y + j, existing)) {
                            // furniture will not fit, so call this a failure
                            return false;
                        }
                        
                        Cell current = existing.getCell(x + i, y + j);
                        Cell cell = layer.getCell(i, j);
                        if (current != null && cell != null) {
                            // conflict
                            return false;
                        }
                    }
                }
            }
        }
        
        return true;
    }
    
    private boolean inBounds(int x, int y, LocationLayer layer) {
        return x >= 0 && y >= 0 && x < layer.getWidth() && y < layer.getHeight();
    }
    
    private int getCost(TiledMap roomMap) {
        int cost = 0;
        TiledMapTileLayer layer = (TiledMapTileLayer) roomMap.getLayers().get("collision");
        if (layer != null) {
            for (int x = 0; x < layer.getWidth(); x++) {
                for (int y = 0; y < layer.getHeight(); y++) {
                    Cell cell = layer.getCell(x, y);
                    if (cell != null) {
                        cost++;
                    }
                }
            }
        }
        return cost;
    }
    
    private void put(String roomId, Rectangle rect) {
        if (!roomsById.containsKey(roomId)) {
            roomsById.put(roomId, new ArrayList<Rectangle>());
        }
        roomsById.get(roomId).add(rect);
    }
    
    private void removeRoomless(List<Encounter> encounters) {
        Iterator<Encounter> it = encounters.iterator();
        while (it.hasNext()) {
            Encounter encounter = it.next();
            if (encounter.getRoomIdCount() == 0) {
                it.remove();
            }
        }
    }
    
    private Room lookupRoom(String roomId) {
        try {
            return availableRooms.get(roomId);
        } catch (Exception ex) {
            InvokenGame.error("Failed to load room: " + roomId, ex);
            return null;
        }
    }
    
    public static RoomType get(Room.Size size) {
        return sizeToType.get(size);
    }
}
