package com.eldritch.invoken.location;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import com.eldritch.invoken.activators.DoorActivator;
import com.eldritch.invoken.actor.items.Credential;
import com.eldritch.invoken.actor.items.Item;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.proto.Locations.Room;
import com.google.common.base.Optional;
import com.google.common.collect.Sets;

public class ConnectedRoom {
	public enum Type {
		Chamber, Hall
	}
	
	private final Set<ConnectedRoom> neighbors = new LinkedHashSet<ConnectedRoom>();
	private final Set<NaturalVector2> points = new LinkedHashSet<NaturalVector2>();
	private final Set<NaturalVector2> chokePoints = new LinkedHashSet<NaturalVector2>();
	private final Set<Agent> residents = new LinkedHashSet<>();
	private final Set<DoorActivator> doors = new LinkedHashSet<>();
	private final Room room;
	private final Type type;
	private final NaturalVector2 center;
	private final Item key;
	
	private Optional<String> faction = Optional.absent();
	
	public ConnectedRoom(Room room, Type type, Collection<NaturalVector2> points) {
	    this.room = room;
		this.type = type;
		this.points.addAll(points);
		center = calculateCenter();
		this.key = Credential.from(hashCode(), room);
	}
	
	public void addDoor(DoorActivator door) {
	    doors.add(door);
	}
	
	public void setLocked(boolean locked) {
	    for (DoorActivator door : doors) {
	        door.setLocked(locked);
	    }
	}
	
	public boolean hasResident() {
	    for (Agent agent : residents) {
	        if (contains(agent.getNaturalPosition())) {
	            // resident is in this room
	            return true;
	        }
	    }
	    return false;
	}
	
	public boolean hasHostileResident(Agent other) {
	    for (Agent agent : residents) {
            if (agent.isAlive() && contains(agent.getNaturalPosition()) && agent.isEnemy(other)) {
                // resident is in this room and hostile to other
                return true;
            }
        }
        return false;
	}
	
	public void addResident(Agent resident) {
	    residents.add(resident);
	}
	
	public boolean contains(NaturalVector2 point) {
	    return points.contains(point);
	}
	
	public Item getKey() {
	    return key;
	}
	
	public boolean isChamber() {
	    return type == Type.Chamber;
	}
	
	public void setFaction(String faction) {
	    this.faction = Optional.of(faction);
	}
	
	public Optional<String> getFaction() {
	    return faction;
	}
	
	public NaturalVector2 getCenter() {
	    return center;
	}
	
	public Set<NaturalVector2> getPoints() {
	    return points;
	}
	
	public Set<NaturalVector2> getChokePoints() {
        return chokePoints;
    }
	
	public Set<NaturalVector2> getAllPoints() {
	    Set<NaturalVector2> all = Sets.newHashSet();
	    all.addAll(points);
	    all.addAll(chokePoints);
	    return all;
	}
	
	public void addChokePoints(Collection<NaturalVector2> points) {
	    this.chokePoints.addAll(points);
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
