package com.eldritch.invoken.encounter.proc;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Rectangle;

public class Room {
    private final List<Room> adjacentRooms = new ArrayList<Room>();
    private final Leaf leaf;
    private int doors = 0;
    
    public Room(Leaf leaf) {
        this.leaf = leaf;
    }
    
    public int getX() {
        return leaf.x;
    }
    
    public int getY() {
        return leaf.y;
    }
    
    public int getWidth() {
        return leaf.width;
    }
    
    public int getHeight() {
        return leaf.height;
    }
    
    public Leaf getLeaf() {
        return leaf;
    }
    
    public Rectangle getBounds() {
        return leaf.room;
    }
    
    public int getDoorCount() {
        return doors;
    }
    
    public void addAdjacentRoom(Room room) {
        adjacentRooms.add(room);
    }
    
    public Iterable<Room> getAdjacentRooms() {
        return adjacentRooms;
    }
}
