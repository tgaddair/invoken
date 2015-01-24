package com.eldritch.invoken.encounter;

import java.util.ArrayList;
import java.util.List;

public class Room {
	enum Type {
		Room, Hall
	}
	
	private final List<Room> neighbors = new ArrayList<Room>();
	private final Type type;
	
	public Room(Type type) {
		this.type = type;
	}
	
	public void addNeighbor(Room neighbor) {
		neighbors.add(neighbor);
	}
}
