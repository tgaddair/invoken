package com.eldritch.invoken.encounter.proc;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.math.Rectangle;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.encounter.layer.LocationMap;
import com.eldritch.invoken.proto.Locations.Encounter;
import com.eldritch.invoken.proto.Locations.Room;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class RoomGenerator {
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
        RoomType type = get(room.getSize());
        // TODO: load furniture
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
