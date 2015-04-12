package com.eldritch.invoken.location.proc;

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
import com.eldritch.invoken.location.proc.RoomDecorator.RoomType;
import com.eldritch.invoken.proto.Locations.ControlPoint;
import com.eldritch.invoken.proto.Locations.Encounter;
import com.eldritch.invoken.proto.Locations.Room;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

public class RoomGenerator extends BspGenerator {
    private final RoomCache roomCache = new RoomCache();
    private final List<ControlPoint> points = new ArrayList<>();
    private final Map<Rectangle, ControlRoom> controlRooms = new LinkedHashMap<>();

    public RoomGenerator(int roomCount, List<ControlPoint> points, long seed) {
        super(roomCount, seed);
        this.points.addAll(points);
    }

    public Collection<ControlRoom> getEncounterRooms() {
        return controlRooms.values();
    }
    
    public void associate(List<Encounter> encounters) {
        // TODO
    }

    @Override
    protected void PlaceRooms() {
        InvokenGame.log("Room Count: " + getRoomCount());

//        // place all encounters at least once first
//        List<Encounter> repeated = new ArrayList<Encounter>();
//        int count = 0;
//        for (ControlPoint cp : points) {
//            if (cp.getOrigin()) {
//                if (place(cp)) {
//                    count++;
//                }
//            } else {
//                repeatedEncounters.add(cp);
//            }
//        }
//
//        if (!repeatedEncounters.isEmpty()) {
//            // place encounters randomly
//            EncounterSelector selector = new EncounterSelector(repeatedEncounters);
//            int remaining = getRoomCount() - count;
//            // InvokenGame.log("Remaining: " + remaining);
//            while (remaining > 0) {
//                ControlPoint encounter = selector.select();
//                place(encounter);
//                remaining--;
//            }
//        }
        
        for (ControlPoint cp : points) {
            int count = (int) (random() * (cp.getMax() - cp.getMin())) + cp.getMin();
            for (int i = 0; i < count; i++) {
                place(cp);
            }
        }
    }

    @Override
    protected void PlaceTunnels() {
        // save("no-tunnels");

        // first, generate the dependency graph from all the encounter-room pairs
        ControlNode origin = generateDependencyGraph(controlRooms.values());

        LinkedList<ControlNode> unlocked = new LinkedList<ControlNode>(); // can place
        List<ControlNode> connectedSample = new ArrayList<ControlNode>(); // can connect to
        Set<ControlNode> connected = new LinkedHashSet<ControlNode>();

        // seed the routine so we can connect to the origin, and we connected from a child
        connectedSample.add(origin);
        connected.add(origin);
        for (ControlNode lock : origin.locks) {
            unlocked.add(lock);
        }

        CostMatrix costs = new EncounterCostMatrix(getWidth(), getHeight(), controlRooms.values());
        while (!unlocked.isEmpty()) {
            ControlNode current = unlocked.removeFirst();
            if (connected.contains(current)) {
                // already placed
                continue;
            }

            ControlNode connection = connectedSample.get((int) (random() * connected.size()));
            DigTunnel(connection.getBounds(), current.getBounds(), costs);

            // add this node to the connected set, and maybe add its children if all its keys
            // are also in the connected set
            connectedSample.add(current);
            connected.add(current);
            for (ControlNode lock : current.locks) {
                if (connected.contains(lock)) {
                    // already placed
                    continue;
                }

                // iterate over all dependencies and check that they've already been placed
                boolean canConnect = true;
                for (ControlNode key : lock.keys) {
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
                connected.size() == controlRooms.size(),
                String.format("expected %d connection, found %d", controlRooms.size(),
                        connected.size()));

        // now that we're done placing tunnels, we need to reconstruct the walls around our
        // encounter rooms, if they're supposed to be locked
        // rebuildWalls();
    }

    private static class EncounterCostMatrix implements CostMatrix {
        private final ControlRoom[][] rooms;

        public EncounterCostMatrix(int width, int height, Collection<ControlRoom> list) {
            rooms = new ControlRoom[width][height];
            for (ControlRoom room : list) {
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
            ControlRoom room = rooms[x][y];
            if (room != null) {
                return getCost(room);
            }
            return 0;
        }

        private static int getCost(ControlRoom room) {
            int cost = 0;

            // origin should be somewhat costly to pass through to reduce traffic
            ControlPoint cp = room.getControlPoint();
            if (cp.getOrigin()) {
                cost += 250;
            }

            // if the room has a dependency, then the cost should be very high
            if (cp.hasRequiredKey() && !cp.getRequiredKey().isEmpty()) {
                cost += 1000;
            }

            // if the room is closed, then the cost should be very high
            if (cp.getLockStrength() > 0) {
                cost += 500;
            }

            return cost;
        }
    }

    private boolean place(ControlPoint cp) {
        // InvokenGame.log("Place: " + encounter.getId());
        int count = 0;
        while (count < 1000) {
            if (cp.getRoomIdList().isEmpty()) {
                int width = range(MinRoomSize, MaxRoomSize);
                int height = range(MinRoomSize, MaxRoomSize);
                Rectangle rect = PlaceRectRoom(width, height);
                if (rect != null) {
                    controlRooms.put(rect, new ControlRoom(cp,
                            Room.getDefaultInstance(), rect));
                    return true;
                }
            } else {
                for (String roomId : cp.getRoomIdList()) {
                    Room room = roomCache.lookupRoom(roomId);
                    RoomType type = RoomDecorator.get(room.getSize());

                    int width = range(type);
                    int height = range(type);
                    Rectangle rect = PlaceRectRoom(width, height);
                    if (rect != null) {
                        controlRooms.put(rect, new ControlRoom(cp, room, rect));
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

    private static ControlNode generateDependencyGraph(Collection<ControlRoom> encounters) {
        // scan through the list and pick out the origin
        List<ControlNode> nodes = new ArrayList<ControlNode>();
        ControlNode origin = null;
        for (ControlRoom encounter : encounters) {
            ControlNode node = new ControlNode(encounter);
            if (node.isOrigin()) {
                origin = node;
            }
            nodes.add(node);
        }
        Preconditions.checkState(origin != null, "Origin must be provided in the encounter list");

        // add the origin as a key of all unlocked encounters
        Map<String, List<ControlNode>> keys = new LinkedHashMap<String, List<ControlNode>>();
        for (ControlNode node : nodes) {
            if (!node.isOrigin() && !node.isLocked()) {
                node.addKey(origin);
                origin.addLock(node);
            }

            // add keys
            for (String key : node.getAvailableKeys()) {
                if (!keys.containsKey(key)) {
                    keys.put(key, new ArrayList<ControlNode>());
                }
                keys.get(key).add(node);
            }
        }

        // finally, add normal dependencies
        for (ControlNode node : nodes) {
            if (!node.isOrigin() && node.isLocked()) {
                Preconditions
                        .checkArgument(keys.containsKey(node.getLock()), String.format(
                                "No key for lock %s, encounter %s", node.getLock(),
                                node.cp.getId()));
                
                for (ControlNode key : keys.get(node.getLock())) {
                    node.addKey(key);
                    key.addLock(node);
                }
            }
        }

        return origin;
    }

    public static class ControlRoom {
        private final ControlPoint cp;
        private final Room room;
        private final Rectangle bounds;

        public ControlRoom(ControlPoint cp, Room room, Rectangle bounds) {
            this.cp = cp;
            this.room = room;
            this.bounds = bounds;
        }

        public ControlPoint getControlPoint() {
            return cp;
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
        
        public Optional<Encounter> chooseEncounter(List<Encounter> encounters) {
            // find all the available encounters for the given control point
            double total = 0;
            List<Encounter> available = new ArrayList<>();
            for (Encounter encounter : encounters) {
                if (encounter.getControlPointIdList().contains(cp.getId())) {
                    total += encounter.getWeight();
                    available.add(encounter);
                }
            }
            
            // sample an encounter with replacement by its weight
            double target = Math.random() * total;
            double sum = 0;
            for (Encounter encounter : available) {
                sum += encounter.getWeight();
                if (sum >= target) {
                    return Optional.of(encounter);
                }
            }
            
            // no encounter found
            return Optional.absent();
        }
    }

    private static class ControlNode {
        // encounters that unlock this one
        private final List<ControlNode> keys = new ArrayList<ControlNode>();

        // encounters that are unlocked by this one
        private final List<ControlNode> locks = new ArrayList<ControlNode>();

        private final ControlRoom controlRoom;
        private final ControlPoint cp;

        public ControlNode(ControlRoom controlRoom) {
            this.controlRoom = controlRoom;
            this.cp = controlRoom.cp;
        }

        public void addKey(ControlNode key) {
            keys.add(key);
        }

        public void addLock(ControlNode lock) {
            locks.add(lock);
        }

        public boolean isOrigin() {
            return cp.getOrigin();
        }

        public boolean isLocked() {
            return cp.hasRequiredKey() && !cp.getRequiredKey().isEmpty();
        }

        public String getLock() {
            return cp.getRequiredKey();
        }

        public List<String> getAvailableKeys() {
            return cp.getAvailableKeyList();
        }

        public Rectangle getBounds() {
            return controlRoom.getBounds();
        }
    }
}
