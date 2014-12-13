package com.eldritch.invoken.encounter.proc;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Rectangle;

public class GeneratedRoom {
    private final List<GeneratedRoom> adjacentRooms = new ArrayList<GeneratedRoom>();
    private final Leaf leaf;
    private int doors = 0;
    
    public GeneratedRoom(Leaf leaf) {
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
    
    public void addAdjacentRoom(GeneratedRoom generatedRoom) {
        adjacentRooms.add(generatedRoom);
    }
    
    public Iterable<GeneratedRoom> getAdjacentRooms() {
        return adjacentRooms;
    }
}
