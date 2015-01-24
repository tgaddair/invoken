package com.eldritch.invoken.encounter;

import java.util.HashSet;
import java.util.Set;

public class ConnectedRoom {
	public enum Type {
		Chamber, Hall
	}
	
	private final Set<ConnectedRoom> neighbors = new HashSet<ConnectedRoom>();
	private final Type type;
	
	public ConnectedRoom(Type type) {
		this.type = type;
	}
	
	public void addNeighbor(ConnectedRoom neighbor) {
		neighbors.add(neighbor);
	}
	
	public boolean isConnected(NaturalVector2 point, ConnectedRoom[][] rooms) {
	    ConnectedRoom room = rooms[point.x][point.y];
	    return room == null || room == this || neighbors.contains(room);
	}
}
