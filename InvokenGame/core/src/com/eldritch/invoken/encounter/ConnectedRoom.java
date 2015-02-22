package com.eldritch.invoken.encounter;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConnectedRoom {
	public enum Type {
		Chamber, Hall
	}
	
	private final Set<ConnectedRoom> neighbors = new HashSet<ConnectedRoom>();
	private final Set<NaturalVector2> points = new HashSet<NaturalVector2>();
	private final Type type;
	private final NaturalVector2 center;
	
	public ConnectedRoom(Type type, Collection<NaturalVector2> points) {
		this.type = type;
		this.points.addAll(points);
		center = calculateCenter();
	}
	
	public NaturalVector2 getCenter() {
	    return center;
	}
	
	public void addPoint(int x, int y) {
	    points.add(NaturalVector2.of(x, y));
	}
	
	public void removePoint(int x, int y) {
	    points.remove(NaturalVector2.of(x, y));
	}
	
	public void addNeighbor(ConnectedRoom neighbor) {
		neighbors.add(neighbor);
	}
	
	public Set<ConnectedRoom> getNeighbors() {
	    return neighbors;
	}
	
	public boolean isConnected(NaturalVector2 point, ConnectedRoom[][] rooms) {
	    ConnectedRoom room = rooms[point.x][point.y];
	    return room == null || room == this || neighbors.contains(room);
	}
	   
    private NaturalVector2 calculateCenter() {
        if (points.isEmpty()) {
            // avoid dividing by zero
            return NaturalVector2.of(0, 0);
        }
        
        int sumX = 0;
        int sumY = 0;
        for (NaturalVector2 point : points) {
            sumX += point.x;
            sumY += point.y;
        }
        return NaturalVector2.of(sumX / points.size(), sumY / points.size());
    }
}
