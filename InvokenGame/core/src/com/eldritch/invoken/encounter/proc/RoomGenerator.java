package com.eldritch.invoken.encounter.proc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.math.Rectangle;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.encounter.NaturalVector2;
import com.eldritch.invoken.encounter.layer.LocationMap;
import com.eldritch.invoken.encounter.proc.FurnitureLoader.PlaceableFurniture;
import com.eldritch.invoken.proto.Locations.Encounter;
import com.eldritch.invoken.proto.Locations.Room;
import com.eldritch.invoken.proto.Locations.Room.Furniture;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class RoomGenerator {
    // threshold of furniture to open ground in room, past which we need to stop adding furniture
    public static final double MAX_FURNITURE = 0.2;
    
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
    
    /**
     * TODO: better way to do encounter and room matching.
     * 
     *  1. Build a dependency graph of encounters starting from the player entrance to the exit. 
     *     Some encounters will be marked as "keys" to others, which implies a dependency.  If a
     *     dependency exists, we must provide a locked door for the depending encounter whose
     *     credential is provided by the key.  More than one encounter can be a key, implying an
     *     "any of" option.
     *  2. Choose a room as the origin, or player teleport location.  The encounter will need to be
     *     explicitly marked as the 'origin' to resolve any ambiguities from multiple teleport
     *     locations.
     *  3. Our encounter placement algorithm will then navigate the room graph from the origin and
     *     place a required encounter in each room.  The room graph will need to tell us (in the
     *     directed edge) if there is a lock going from our current room to the next room.  If
     *     there is a lock, then we can place exactly ONE dependent encounter in the subtree of
     *     the room graph behind this locked door.  Once we've placed a locked encounter, we can
     *     consider placing all the encounters that depend on it.  In this way, we traverse the
     *     room graph in much the same way as the player would, which is why it works.
     *  4. If any unplaced required encounters exist...
     */
    public void generate(List<Rectangle> bounds, List<Encounter> encounters) {
        List<Encounter> available = new ArrayList<Encounter>(encounters);
        
        // remove any encounters that don't have a room specified, as they can go anywhere
        removeRoomless(available);
        
        // sort the encounters by priority so more important encounters get first dibs on rooms
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
        List<Furniture> availableFurniture = new ArrayList<Furniture>(room.getFurnitureList());
        Collections.shuffle(availableFurniture);
        for (Furniture furniture : availableFurniture) {
            PlaceableFurniture placeable = FurnitureLoader.load(furniture);
            
            // calculate the percentage of furniture coverage to ground tiles adding this piece of
            // furniture would cost us
            int cost = placeable.getCost();
            double coverage = (coveredTiles + cost) / area;
            
            // find a suitable place in room that satisfies the constraints
            if (coverage < MAX_FURNITURE) {
                NaturalVector2 position = placeable.findPosition(rect, map);
                if (position != null) {
                    // found a place to put the furniture, so merge it into the map
                    placeable.place(position, map);
                    coveredTiles += cost;
                }
            }
        }
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
