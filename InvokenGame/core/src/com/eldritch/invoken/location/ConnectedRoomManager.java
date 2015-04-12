package com.eldritch.invoken.location;

import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Set;

import com.eldritch.invoken.location.proc.RoomGenerator.ControlRoom;
import com.google.common.collect.ImmutableBiMap;

public class ConnectedRoomManager {
    private final ConnectedRoom[][] rooms;
    private final Set<ConnectedRoom> roomSet = new LinkedHashSet<ConnectedRoom>();
    private ImmutableBiMap<ControlRoom, ConnectedRoom> roomMap = ImmutableBiMap.of();

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
        roomSet.add(room);
    }

    public ConnectedRoom getRoom(int x, int y) {
        return rooms[x][y];
    }

    public Set<ConnectedRoom> getRooms() {
        return roomSet;
    }

    public boolean hasRoom(int x, int y) {
        if (x < 0 || y < 0 || x >= rooms.length || y >= rooms[x].length) {
            // out of bounds
            return false;
        }

        return rooms[x][y] != null;
    }

    public void setMapping(ImmutableBiMap<ControlRoom, ConnectedRoom> mapping) {
        // every encounter will have exactly one connected room
        // every connected room will have at most one encounter (only chambers)
        // immutable so we can maintain deterministic iteration order
        roomMap = mapping;
    }

    public ConnectedRoom getConnected(ControlRoom encounter) {
        return roomMap.get(encounter);
    }

    public ControlRoom getEncounter(ConnectedRoom connected) {
        return roomMap.inverse().get(connected);
    }

    public boolean hasEncounter(ConnectedRoom connected) {
        return roomMap.inverse().containsKey(connected);
    }

    public Set<ControlRoom> getEncounters() {
        return roomMap.keySet();
    }

    public Set<Entry<ControlRoom, ConnectedRoom>> getChambers() {
        return roomMap.entrySet();
    }

    public ConnectedRoom[][] getGrid() {
        return rooms;
    }
}
