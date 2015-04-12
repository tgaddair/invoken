package com.eldritch.invoken.actor.pathfinding;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.indexed.IndexedNode;
import com.badlogic.gdx.utils.Array;
import com.eldritch.invoken.location.ConnectedRoom;
import com.eldritch.invoken.location.NaturalVector2;

public abstract class LocationNode implements IndexedNode<LocationNode> {
	public final NaturalVector2 position;
	private final Array<Connection<LocationNode>> connections;
	private final int index;

	public LocationNode(NaturalVector2 position, int index) {
		this.position = position;
		this.connections = new Array<Connection<LocationNode>>();
		this.index = index;
	}
	
	public int getX() {
		return position.x;
	}
	
	public int getY() {
		return position.y;
	}

	@Override
	public int getIndex() {
		return index;
	}

	@Override
	public Array<Connection<LocationNode>> getConnections() {
		return this.connections;
	}
	
    public abstract LocationNode getLowerLevelNode();
    
    public abstract LocationNode getUpperLevelNode();
    
    public static class TiledNode extends LocationNode {
        private final ConnectedRoom room;
        private final LocationGraph graph;
        private final boolean ground;
        
        public TiledNode(ConnectedRoom room, NaturalVector2 position, boolean ground, int index, LocationGraph graph) {
            super(position, index);
            this.room = room;
            this.graph = graph;
            this.ground = ground;
        }
        
        public boolean isGround() {
            return ground;
        }

        @Override
        public LocationNode getLowerLevelNode() {
            // nothing below
            return null;
        }

        @Override
        public LocationNode getUpperLevelNode() {
            if (room == null) {
                return graph.getDefaultRoomNode();
            }
            
            // containing room
            return graph.getNode(room);
        }
    }
	
	public static class RoomNode extends LocationNode {
	    private final NaturalVector2 closest;
	    private final LocationGraph graph;
	    
        public RoomNode(NaturalVector2 center, NaturalVector2 closest, int index, LocationGraph graph) {
            super(center, index);
            this.closest = closest;
            this.graph = graph;
        }

        @Override
        public LocationNode getLowerLevelNode() {
            // center point in room
            return graph.getNode(closest);
        }

        @Override
        public LocationNode getUpperLevelNode() {
            // nothing above
            return null;
        }
	}
	
//	public static TileNode create(NaturalVector2 position, LocationMap map) {
//		Array<Connection<TileNode>> connections = new Array<Connection<TileNode>>();
//		for (int dx = -1; dx <= 1; dx++) {
//			for (int dy = -1; dy <= 1; dy++) {
//				if (dx == 0 && dy == 0) {
//					continue;
//				}
//				
//				if (map.isClearGround(position.x + dx, position.y + dy)) {
//					Connection<TileNode> 
//					connections.add(connection);
//				}
//			}
//		}
//		
//		int index = position.y * map.getWidth() + position.x;
//		return new TileNode(position, connections, index);
//	}
}