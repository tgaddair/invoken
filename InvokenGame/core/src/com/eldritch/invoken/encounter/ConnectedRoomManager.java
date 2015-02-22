package com.eldritch.invoken.encounter;

import java.util.Map.Entry;
import java.util.Set;

import com.eldritch.invoken.encounter.proc.EncounterGenerator.EncounterRoom;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class ConnectedRoomManager {
    private final ConnectedRoom[][] rooms;
    private final BiMap<EncounterRoom, ConnectedRoom> roomMap = HashBiMap.create();

    public ConnectedRoomManager(int width, int height) {
        rooms = new ConnectedRoom[width][height];
    }

    public void setRoom(int x, int y, ConnectedRoom room) {
        if (rooms[x][y] != null) {
            // remove current room
            rooms[x][y].removePoint(x, y);
        }

        // update bi-directional relation
        rooms[x][y] = room;
        room.addPoint(x, y);
    }

    public ConnectedRoom getRoom(int x, int y) {
        return rooms[x][y];
    }

    public boolean hasRoom(int x, int y) {
        return rooms[x][y] != null;
    }
    
    public void addMapping(EncounterRoom encounter, ConnectedRoom connected) {
        // every encounter will have exactly one connected room
        // every connected room will have at most one encounter (only chambers)
        roomMap.put(encounter, connected);
    }
    
    public ConnectedRoom getConnected(EncounterRoom encounter) {
        return roomMap.get(encounter);
    }
    
    public EncounterRoom getEncounter(ConnectedRoom connected) {
        return roomMap.inverse().get(connected);
    }
    
    public boolean hasEncounter(ConnectedRoom connected) {
        return roomMap.inverse().containsKey(connected);
    }
    
    public Set<EncounterRoom> getEncounters() {
        return roomMap.keySet();
    }
    
    public Set<Entry<EncounterRoom, ConnectedRoom>> getChambers() {
        return roomMap.entrySet();
    }

    public ConnectedRoom[][] getGrid() {
        return rooms;
    }
}
