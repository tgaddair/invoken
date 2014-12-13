package com.eldritch.invoken.encounter.proc;

import java.util.EnumMap;

import com.badlogic.gdx.math.Rectangle;
import com.eldritch.invoken.encounter.layer.LocationMap;
import com.eldritch.invoken.proto.Locations.Room;

public class RoomGenerator {
    enum RoomType {
        SMALL(0, 7), MEDIUM(6, 10), LARGE(9, Integer.MAX_VALUE);

        // the square-root of the area, or the length of one dimension of a square room
        private final int min;
        private final int max;

        private RoomType(int min, int max) {
            this.min = min;
            this.max = max;
        }
    }

    private static final EnumMap<Room.Size, RoomType> sizeToType = new EnumMap<Room.Size, RoomType>(
            Room.Size.class);
    
    static {
        sizeToType.put(Room.Size.SMALL, RoomType.SMALL);
        sizeToType.put(Room.Size.MEDIUM, RoomType.MEDIUM);
        sizeToType.put(Room.Size.LARGE, RoomType.LARGE);
    }
    
    public static void generate(Room room, Rectangle bounds, LocationMap map) {
        RoomType type = sizeToType.get(room.getSize());
    }
}
