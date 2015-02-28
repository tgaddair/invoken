package com.eldritch.invoken.encounter.proc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import com.badlogic.gdx.math.Rectangle;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.encounter.proc.RoomGenerator.RoomType;
import com.eldritch.invoken.proto.Locations.Encounter;
import com.eldritch.invoken.proto.Locations.Room;
import com.google.common.base.Preconditions;

public class EncounterGenerator extends BspGenerator {
    private final RoomCache roomCache = new RoomCache();
    private final List<Encounter> encounters = new ArrayList<Encounter>();
    private final Map<Rectangle, EncounterRoom> encounterRooms = new LinkedHashMap<Rectangle, EncounterRoom>();

    public EncounterGenerator(int roomCount, List<Encounter> encounters, long seed) {
        super(roomCount, seed);
        this.encounters.addAll(encounters);
    }

    public Collection<EncounterRoom> getEncounterRooms() {
        return encounterRooms.values();
    }

    @Override
    protected void PlaceRooms() {
        InvokenGame.log("Room Count: " + getRoomCount());

        // place all encounters at least once first
        List<Encounter> repeatedEncounters = new ArrayList<Encounter>();
        int count = 0;
        for (Encounter encounter : encounters) {
            if (encounter.getOrigin() || encounter.getUnique()) {
                if (place(encounter)) {
                    count++;
                }
            } else {
                repeatedEncounters.add(encounter);
            }
        }

        if (!repeatedEncounters.isEmpty()) {
            // place encounters randomly
            EncounterSelector selector = new EncounterSelector(repeatedEncounters);
            int remaining = getRoomCount() - count;
//            InvokenGame.log("Remaining: " + remaining);
            while (remaining > 0) {
                Encounter encounter = selector.select();
                place(encounter);
                remaining--;
            }
        }
    }

    @Override
    protected void PlaceTunnels() {
//        save("no-tunnels");

        // first, generate the dependency graph from all the encounter-room pairs
        EncounterNode origin = generateDependencyGraph(encounterRooms.values());

        LinkedList<EncounterNode> unlocked = new LinkedList<EncounterNode>(); // can place
        List<EncounterNode> connectedSample = new ArrayList<EncounterNode>(); // can connect to
        Set<EncounterNode> connected = new LinkedHashSet<EncounterNode>();

        // seed the routine so we can connect to the origin, and we connected from a child
        connectedSample.add(origin);
        connected.add(origin);
        for (EncounterNode lock : origin.locks) {
            unlocked.add(lock);
        }

        CostMatrix costs = new EncounterCostMatrix(getWidth(), getHeight(), encounterRooms.values());
        while (!unlocked.isEmpty()) {
            EncounterNode current = unlocked.removeFirst();
            if (connected.contains(current)) {
                // already placed
                continue;
            }

            EncounterNode connection = connectedSample
                    .get((int) (random() * connected.size()));
            DigTunnel(connection.getBounds(), current.getBounds(), costs);

            // add this node to the connected set, and maybe add its children if all its keys
            // are also in the connected set
            connectedSample.add(current);
            connected.add(current);
            for (EncounterNode lock : current.locks) {
                if (connected.contains(lock)) {
                    // already placed
                    continue;
                }

                // iterate over all dependencies and check that they've already been placed
                boolean canConnect = true;
                for (EncounterNode key : lock.keys) {
                    if (!connected.contains(key)) {
                        canConnect = false;
                        break;
                    }
                }

                // all dependencies placed, so add this one to the unlocked set
                if (canConnect) {
                    unlocked.add(lock);
                }
            }
        }

        // finally, asset that all the encounters were connected
        Preconditions.checkState(
                connected.size() == encounterRooms.size(),
                String.format("expected %d connection, found %d", encounterRooms.size(),
                        connected.size()));

        // now that we're done placing tunnels, we need to reconstruct the walls around our
        // encounter rooms, if they're supposed to be locked
//        rebuildWalls();
    }
    
    private static class EncounterCostMatrix implements CostMatrix {
        private final EncounterRoom[][] rooms;
        
        public EncounterCostMatrix(int width, int height, Collection<EncounterRoom> list) {
            rooms = new EncounterRoom[width][height];
            for (EncounterRoom room : list) {
                Rectangle bounds = room.getBounds();
                
                // boundary of the chamber, the stone border goes 1 unit out of the bounds
                int startX = (int) bounds.x - 1;
                int endX = (int) (bounds.x + bounds.width);
                int startY = (int) bounds.y - 1;
                int endY = (int) (bounds.y + bounds.height);

                // the endpoints are exclusive, as a rectangle at (0, 0) with size
                // (1, 1) should cover
                // only rooms[0][0], not rooms[1][1]
                for (int x = startX; x <= endX; x++) {
                    for (int y = startY; y <= endY; y++) {
                        if (rooms[x][y] == null || getCost(room) > getCost(rooms[x][y])) {
                            // put the highest cost room in the cost matrix
                            rooms[x][y] = room;
                        }
                    }
                }
            }
        }
        
        public int getCost(int x, int y) {
            EncounterRoom room = rooms[x][y];
            if (room != null) {
                return getCost(room);
            }
            return 0;
        }
        
        private static int getCost(EncounterRoom room) {
            int cost = 0;
            
            // origin should be somewhat costly to pass through to reduce traffic
            if (room.getEncounter().getOrigin()) {
                cost += 250;
            }
            
            // if the room has a dependency, then the cost should be very high
            if (room.getEncounter().hasRequiredKey() 
                    && !room.getEncounter().getRequiredKey().isEmpty()) {
                cost += 1000;
            }
            
            // if the room is closed, then the cost should be very high
            if (room.getEncounter().getLockStrength() > 0) {
                cost += 500;
            }
            
            return cost;
        }
    }

    private boolean place(Encounter encounter) {
        InvokenGame.log("Place: " + encounter.getId());
        int count = 0;
        while (count < 1000) {
            if (encounter.getRoomIdList().isEmpty()) {
                int width = range(MinRoomSize, MaxRoomSize);
                int height = range(MinRoomSize, MaxRoomSize);
                Rectangle rect = PlaceRectRoom(width, height);
                if (rect != null) {
                    encounterRooms.put(rect, new EncounterRoom(encounter,
                            Room.getDefaultInstance(), rect));
                    return true;
                }
            } else {
                for (String roomId : encounter.getRoomIdList()) {
                    Room room = roomCache.lookupRoom(roomId);
                    RoomType type = RoomGenerator.get(room.getSize());

                    int width = range(type);
                    int height = range(type);
                    Rectangle rect = PlaceRectRoom(width, height);
                    if (rect != null) {
                        encounterRooms.put(rect, new EncounterRoom(encounter, room, rect));
                        return true;
                    }
                }
            }
            count++;
        }

        // TODO: get first available
        return false;
    }

    private int range(RoomType type) {
        int min = Math.max(type.getMin(), MinRoomSize);
        int max = Math.min(type.getMax(), MaxRoomSize);
        return range(min, max);
    }

    public static double getTotalWeight(List<Encounter> encounters) {
        double total = 0.0;
        for (Encounter encounter : encounters) {
            if (encounter.getType() == Encounter.Type.ACTOR) {
                total += encounter.getWeight();
            }
        }
        return total;
    }

    public class EncounterSelector {
        private final double totalWeight;
        private final NavigableSet<WeightedEncounter> selection = new TreeSet<WeightedEncounter>();
        private final WeightedEncounter search = new WeightedEncounter(null, 0);

        public EncounterSelector(List<Encounter> repeatedEncounters) {
            // calculate the cumulative sum map
            double total = 0;
            for (Encounter encounter : repeatedEncounters) {
                total += encounter.getWeight();
                selection.add(new WeightedEncounter(encounter, total));
            }
            totalWeight = total;
        }

        public Encounter select() {
            search.cumulativeWeight = random() * totalWeight;
            return selection.ceiling(search).encounter;
        }
    }

    public static class WeightedEncounter implements Comparable<WeightedEncounter> {
        private final Encounter encounter;
        private double cumulativeWeight;

        public WeightedEncounter(Encounter encounter, double weight) {
            this.encounter = encounter;
            this.cumulativeWeight = weight;
        }

        @Override
        public int compareTo(WeightedEncounter other) {
            return Double.compare(this.cumulativeWeight, other.cumulativeWeight);
        }

    }

    private static EncounterNode generateDependencyGraph(Collection<EncounterRoom> encounters) {
        // scan through the list and pick out the origin
        List<EncounterNode> nodes = new ArrayList<EncounterNode>();
        EncounterNode origin = null;
        for (EncounterRoom encounter : encounters) {
            EncounterNode node = new EncounterNode(encounter);
            if (node.isOrigin()) {
                origin = node;
            }
            nodes.add(node);
        }
        Preconditions.checkState(origin != null, "Origin must be provided in the encounter list");

        // add the origin as a key of all unlocked encounters
        Map<String, List<EncounterNode>> keys = new LinkedHashMap<String, List<EncounterNode>>();
        for (EncounterNode node : nodes) {
            if (!node.isOrigin() && !node.isLocked()) {
                node.addKey(origin);
                origin.addLock(node);
            }

            // add keys
            for (String key : node.getAvailableKeys()) {
                if (!keys.containsKey(key)) {
                    keys.put(key, new ArrayList<EncounterNode>());
                }
                keys.get(key).add(node);
            }
        }

        // finally, add normal dependencies
        for (EncounterNode node : nodes) {
            if (!node.isOrigin() && node.isLocked()) {
                for (EncounterNode key : keys.get(node.getLock())) {
                    node.addKey(key);
                    key.addLock(node);
                }
            }
        }

        return origin;
    }

    public static class EncounterRoom {
        private final Encounter encounter;
        private final Room room;
        private final Rectangle bounds;

        public EncounterRoom(Encounter encounter, Room room, Rectangle bounds) {
            this.encounter = encounter;
            this.room = room;
            this.bounds = bounds;
        }

        public Encounter getEncounter() {
            return encounter;
        }

        public Room getRoom() {
            return room;
        }

        public Rectangle getBounds() {
            return bounds;
        }
        
        public Rectangle getRestrictedBounds() {
            Rectangle restricted = new Rectangle(bounds);
            restricted.x += 1;
            restricted.y += 1;
            restricted.width -= 1;
            restricted.height -= 1;
            return restricted;
        }
    }

    private static class EncounterNode {
        // encounters that unlock this one
        private final List<EncounterNode> keys = new ArrayList<EncounterNode>();

        // encounters that are unlocked by this one
        private final List<EncounterNode> locks = new ArrayList<EncounterNode>();

        private final EncounterRoom encounterRoom;
        private final Encounter encounter;

        public EncounterNode(EncounterRoom encounterRoom) {
            this.encounterRoom = encounterRoom;
            this.encounter = encounterRoom.encounter;
        }

        public void addKey(EncounterNode key) {
            keys.add(key);
        }

        public void addLock(EncounterNode lock) {
            locks.add(lock);
        }

        public boolean isOrigin() {
            return encounter.getOrigin();
        }

        public boolean isLocked() {
            return encounter.hasRequiredKey() && !encounter.getRequiredKey().isEmpty();
        }

        public String getLock() {
            return encounter.getRequiredKey();
        }

        public List<String> getAvailableKeys() {
            return encounter.getAvailableKeyList();
        }

        public Rectangle getBounds() {
            return encounterRoom.getBounds();
        }
    }
}
