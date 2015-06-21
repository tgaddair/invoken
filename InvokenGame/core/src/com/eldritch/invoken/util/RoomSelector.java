package com.eldritch.invoken.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.proto.Locations.Room;

public class RoomSelector {
    private final List<Room> genericRooms = new ArrayList<>();
    
    public List<Room> getGenericRooms(Random rand) {
        List<Room> shuffled = new ArrayList<>(genericRooms);
        Collections.shuffle(shuffled, rand);
        return shuffled;
    }
    
    public void load() {
        genericRooms.clear();
        for (Room room : InvokenGame.ROOM_READER.readAll()) {
            if (!room.getUnique()) {
                genericRooms.add(room);
            }
        }
    }
}
