package com.eldritch.invoken.location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eldritch.invoken.proto.Locations.Room;

public class RoomIndex {
    private final Map<Room.Type, List<ConnectedRoom>> index = new HashMap<>();
    
    public boolean hasRoom(Room.Type type) {
        return index.containsKey(type) && !index.get(type).isEmpty();
    }
    
    public List<ConnectedRoom> getRooms(Room.Type type) {
        return index.get(type);
    }
    
    public ConnectedRoom getRandomRoom(Room.Type type) {
        List<ConnectedRoom> rooms = index.get(type);
        return rooms.get((int) (Math.random() * rooms.size()));
    }
    
    public ConnectedRoom getRoomInTerritory(Room.Type type, String factionId) {
        List<ConnectedRoom> rooms = new ArrayList<>();
        for (ConnectedRoom room : index.get(type)) {
            if (room.getFaction().isPresent() && room.getFaction().get().equals(factionId)) {
                rooms.add(room);
            }
        }
        return rooms.get((int) (Math.random() * rooms.size()));
    }
}
