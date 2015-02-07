package com.eldritch.invoken.encounter.proc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

import com.badlogic.gdx.math.Rectangle;
import com.eldritch.invoken.encounter.NaturalVector2;
import com.eldritch.invoken.encounter.layer.LocationMap;
import com.eldritch.invoken.encounter.proc.EncounterGenerator.EncounterRoom;
import com.eldritch.invoken.encounter.proc.FurnitureLoader.PlaceableFurniture;
import com.eldritch.invoken.proto.Locations.Room;
import com.eldritch.invoken.proto.Locations.Room.Furniture;

public class RoomGenerator {
    // threshold of furniture to open ground in room, past which we need to stop adding furniture
    public static final double MAX_FURNITURE = 0.2;
    
    enum RoomType {
        SMALL(0, 7), MEDIUM(6, 10), LARGE(9, Integer.MAX_VALUE);

        private final int min;
        private final int max;

        private RoomType(int min, int max) {
            this.min = min;
            this.max = max;
        }
        
        public int getMin() {
            return min;
        }
        
        public int getMax() {
            return max;
        }
        
        public boolean fitsBounds(Rectangle bounds) {
            float area = bounds.area();
            return area >= (min * min) && area <= (max * max);
        }
    }

    private static final EnumMap<Room.Size, RoomType> sizeToType = new EnumMap<Room.Size, RoomType>(
            Room.Size.class);
    
    static {
        sizeToType.put(Room.Size.SMALL, RoomType.SMALL);
        sizeToType.put(Room.Size.MEDIUM, RoomType.MEDIUM);
        sizeToType.put(Room.Size.LARGE, RoomType.LARGE);
    }
    
    private final LocationMap map;
    
    public RoomGenerator(LocationMap map) {
        this.map = map;
    }
    
    public void generate(EncounterGenerator generator) {
        for (EncounterRoom encounter : generator.getEncounterRooms()) {
            place(encounter);
        }
    }
    
    private void place(EncounterRoom encounter) {
        Rectangle bounds = encounter.getBounds();
        Room room = encounter.getRoom();
        
        double area = bounds.area();
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
                NaturalVector2 position = placeable.findPosition(bounds, map);
                if (position != null) {
                    // found a place to put the furniture, so merge it into the map
                    placeable.place(position, map);
                    coveredTiles += cost;
                }
            }
        }
    }
    
    public static RoomType get(Room.Size size) {
        return sizeToType.get(size);
    }
}
