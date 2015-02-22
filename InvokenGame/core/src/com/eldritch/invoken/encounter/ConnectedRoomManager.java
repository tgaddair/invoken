package com.eldritch.invoken.encounter;

public class ConnectedRoomManager {
    private final ConnectedRoom[][] rooms;
    
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
    
    public ConnectedRoom[][] getGrid() {
        return rooms;
    }
}
