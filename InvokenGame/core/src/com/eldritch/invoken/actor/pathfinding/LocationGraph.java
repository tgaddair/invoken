package com.eldritch.invoken.actor.pathfinding;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.ai.pfa.indexed.IndexedHierarchicalGraph;
import com.eldritch.invoken.actor.pathfinding.LocationNode.RoomNode;
import com.eldritch.invoken.actor.pathfinding.LocationNode.TiledNode;
import com.eldritch.invoken.encounter.ConnectedRoom;
import com.eldritch.invoken.encounter.NaturalVector2;
import com.eldritch.invoken.encounter.layer.LocationMap;

public class LocationGraph extends IndexedHierarchicalGraph<LocationNode> {
    private static final int LEVELS = 1;

    private final Map<NaturalVector2, TiledNode> tiledNodes = new HashMap<NaturalVector2, TiledNode>();
    private final Map<ConnectedRoom, RoomNode> roomNodes = new HashMap<ConnectedRoom, RoomNode>();
    private final RoomNode defaultRoomNode;

    private NaturalVector2 getFirstClear(LocationMap map) {
        for (ConnectedRoom room : map.getRooms().getRooms()) {
            for (NaturalVector2 tile : room.getPoints()) {
                if (map.isClearGround(tile.x, tile.y)) {
                    return tile;
                }
            }
        }
        return null;
    }

    public LocationGraph(LocationMap map) {
        super(LEVELS);

        int index = 0;

        // level 0 -> tiles
        this.level = 0;
        for (int x = 0; x < map.getWidth(); x++) {
            for (int y = 0; y < map.getHeight(); y++) {
                // add a node
                ConnectedRoom room = map.getRooms().getRoom(x, y);
                NaturalVector2 tile = NaturalVector2.of(x, y);
                if (map.isClearGround(x, y)) {
                    TiledNode node = new TiledNode(room, tile, map.isClearGround(x, y), index++, this);
                    tiledNodes.put(tile, node);
                    nodes.add(node);
                }
            }
        }

        // use the 2d continental divide technique to find the approximately closest room

        // level 1 -> rooms
        this.level = 1;
        for (ConnectedRoom room : map.getRooms().getRooms()) {
            NaturalVector2 closest = null;
            for (NaturalVector2 tile : room.getPoints()) {
                if (map.isClearGround(tile.x, tile.y)) {
                    if (closest == null
                            || tile.mdst(room.getCenter()) < closest.mdst(room.getCenter())) {
                        closest = tile;
                    }
                }
            }

            RoomNode node = new RoomNode(room.getCenter(), closest, index++, this);
            roomNodes.put(room, node);
            nodes.add(node);
        }

        // add level 0 connections
        for (TiledNode node : tiledNodes.values()) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0) {
                        continue;
                    }
                    
                    // disallow diagonal movement
                    if ((dx != 0) && (dy != 0)) {
                        continue;
                    }

                    NaturalVector2 neighbor = NaturalVector2.of(node.position.x + dx,
                            node.position.y + dy);
                    if (isGround(neighbor.x, neighbor.y)) {
                        TiledNode target = tiledNodes.get(neighbor);
                        node.getConnections().add(new LocationConnection(node, target));
                    }
                }
            }
        }

        // add level 1 connections
        for (ConnectedRoom room : map.getRooms().getRooms()) {
            RoomNode node = roomNodes.get(room);

            for (ConnectedRoom neighbor : room.getNeighbors()) {
                RoomNode target = roomNodes.get(neighbor);
                node.getConnections().add(new LocationConnection(node, target));
            }
        }
        
      NaturalVector2 first = getFirstClear(map);
//      NaturalVector2 first = NaturalVector2.of(0, 0);
      defaultRoomNode = new RoomNode(first, first, index++, this);
      nodes.add(defaultRoomNode);
    }

    public RoomNode getDefaultRoomNode() {
        return defaultRoomNode;
    }
    
    public boolean isGround(int x, int y) {
        return hasNode(x, y) && tiledNodes.get(NaturalVector2.of(x, y)).isGround();
    }

    public boolean hasNode(int x, int y) {
        return tiledNodes.containsKey(NaturalVector2.of(x, y));
    }

    public TiledNode getNode(NaturalVector2 position) {
        return tiledNodes.get(position);
    }

    public RoomNode getNode(ConnectedRoom room) {
        return roomNodes.get(room);
    }

    @Override
    public LocationNode convertNodeBetweenLevels(int inputLevel, LocationNode node, int outputLevel) {
        if (inputLevel < outputLevel) {
            return node.getUpperLevelNode();
        }

        if (inputLevel > outputLevel) {
            return node.getLowerLevelNode();
        }

        return node;
    }

}
