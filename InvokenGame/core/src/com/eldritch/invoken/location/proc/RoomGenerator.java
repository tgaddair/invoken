package com.eldritch.invoken.location.proc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import com.badlogic.gdx.math.Rectangle;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.location.ConnectedRoomManager;
import com.eldritch.invoken.location.proc.RoomDecorator.RoomType;
import com.eldritch.invoken.proto.Locations.ControlPoint;
import com.eldritch.invoken.proto.Locations.Encounter;
import com.eldritch.invoken.proto.Locations.Room;
import com.eldritch.invoken.util.Pair;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class RoomGenerator extends BspGenerator {
    private final RoomCache roomCache = new RoomCache();
    private final List<Pair<ControlPoint, Integer>> roomCounts = new ArrayList<>();
    private final Map<Rectangle, ControlRoom> controlRooms = new LinkedHashMap<>();
    private final Map<String, List<ControlPoint>> follows = new HashMap<>();

    public RoomGenerator(int roomCount, List<ControlPoint> points,
            List<Pair<ControlPoint, Integer>> roomCounts, long seed) {
        super(roomCount, seed);
        this.roomCounts.addAll(roomCounts);

        // create following
        for (ControlPoint cp : points) {
            for (String followed : cp.getFollowsList()) {
                if (!follows.containsKey(followed)) {
                    follows.put(followed, new ArrayList<ControlPoint>());
                }
                follows.get(followed).add(cp);
            }
        }
    }

    public Collection<ControlRoom> getEncounterRooms() {
        return controlRooms.values();
    }

    @Override
    protected void PlaceRooms() {
        // sort such that we place rooms with a lot of followers first
        Collections.sort(roomCounts, new Comparator<Pair<ControlPoint, Integer>>() {
            @Override
            public int compare(Pair<ControlPoint, Integer> o1, Pair<ControlPoint, Integer> o2) {
                int count1 = follows.containsKey(o1.first.getId()) ? follows.get(o1.first.getId())
                        .size() : 0;
                int count2 = follows.containsKey(o2.first.getId()) ? follows.get(o2.first.getId())
                        .size() : 0;
                return Integer.compare(count2, count1);
            }
        });

        InvokenGame.log("Room Count: " + getRoomCount());
        CostMatrix cost = new DefaultCostMatrix();
        for (Pair<ControlPoint, Integer> elem : roomCounts) {
            ControlPoint cp = elem.first;
            int count = elem.second;
            for (int i = 0; i < count; i++) {
                System.out.println("placing: " + cp.getId());
                place(cp, cost);
            }
        }
    }

    @Override
    protected void PlaceTunnels() {
        // save("no-tunnels");
        int connections = 0;

        // first, generate the dependency graph from all the encounter-room pairs
        ControlNode origin = generateDependencyGraph(controlRooms.values());

        LinkedList<ControlNode> unlocked = new LinkedList<ControlNode>(); // can place
        List<ControlNode> connectedSample = new ArrayList<ControlNode>(); // can connect to
        Set<ControlNode> connected = new LinkedHashSet<ControlNode>();

        // seed the routine so we can connect to the origin, and we connected from a child
        connections++;
        connected.add(origin);
        if (!origin.cp.getClosed()) {
            connectedSample.add(origin);
        }
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

            connections++;
            connected.add(current);
            if (!current.cp.getClosed()) {
                // can connect implicitly
                if (!connectedSample.isEmpty()) {
                    ControlNode connection = connectedSample
                            .get((int) (random() * connectedSample.size()));
                    DigTunnel(connection.getBounds(), current.getBounds(), costs);
                }

                // add this node to the connected set, and maybe add its children if all its keys
                // are also in the connected set
                connectedSample.add(current);
            }

            // unlock dependencies
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

        // finally, assert that all the encounters were connected
        Preconditions
                .checkState(connections == controlRooms.size(), String.format(
                        "expected %d connection, found %d", controlRooms.size(), connections));

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
    
    private Rectangle place(ControlPoint cp, CostMatrix cost) {
        // first, place the room somewhere valid on the map
        Rectangle rect = place(cp);
        
        // next, place and connected to our followers
        placeFollowers(cp, rect, cost);
        return rect;
    }
    
    private void placeFollowers(ControlPoint cp, Rectangle origin, CostMatrix cost) {
        if (follows.containsKey(cp.getId())) {
            for (ControlPoint follower : follows.get(cp.getId())) {
                Rectangle rect = place(follower, origin);
                DigTunnel(origin, rect, cost);
                InvokenGame.log(cp.getId() + " -> " + follower.getId());
                placeFollowers(follower, rect, cost);
            }
        }
    }

    private Rectangle place(ControlPoint cp, Rectangle origin, CostMatrix cost) {
        Rectangle rect = origin == null ? place(cp) : place(cp, origin);

        // following
        if (follows.containsKey(cp.getId())) {
            for (ControlPoint follower : follows.get(cp.getId())) {
                Rectangle rect2 = place(follower, rect, cost);
                // Rectangle rect2 = place(follower, rect);
                DigTunnel(rect, rect2, cost);
//                System.out.println(rect + " -> " + rect2);
                InvokenGame.log(cp.getId() + " -> " + follower.getId());
            }
        }

        return rect;
    }

    private Rectangle place(ControlPoint cp) {
        // InvokenGame.log("Place: " + encounter.getId());
        int count = 0;
        while (count < 1000) {
            if (cp.getRoomIdList().isEmpty()) {
                int width = range(MinRoomSize, MaxRoomSize);
                int height = range(MinRoomSize, MaxRoomSize);
                Rectangle rect = PlaceRectRoom(width, height);
                if (rect != null) {
                    controlRooms.put(rect, new ControlRoom(cp, Room.getDefaultInstance(), rect));
                    return rect;
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
                        return rect;
                    }
                }
            }
            count++;
        }

        // TODO: get first available
        throw new IllegalStateException("Unable to place: " + cp.getId());
    }

    private Rectangle randomRect(Rectangle origin, int dx, int dy, int width, int height) {
        float x = rangeAround((int) origin.x, (int) origin.width, width + dx, getWidth());
        float y = rangeAround((int) origin.y, (int) origin.height, height + dy, getHeight());
        if (random() < 0.5) {
            x = origin.x;
        } else {
            y = origin.y;
        }
        return new Rectangle(x, y, width, height);
    }

    private Rectangle place(ControlPoint cp, Rectangle followed) {
        // InvokenGame.log("Place: " + encounter.getId());
        int dx = 0;
        int dy = 0;
        int count = 0;
        while (count < 1000) {
            if (cp.getRoomIdList().isEmpty()) {
                int width = range(MinRoomSize, MaxRoomSize);
                int height = range(MinRoomSize, MaxRoomSize);
                Rectangle rect = randomRect(followed, dx, dy, width, height);
                if (placeRectRoom(rect)) {
                    controlRooms.put(rect, new ControlRoom(cp, Room.getDefaultInstance(), rect));
                    return rect;
                }
            } else {
                for (String roomId : cp.getRoomIdList()) {
                    Room room = roomCache.lookupRoom(roomId);
                    RoomType type = RoomDecorator.get(room.getSize());

                    int width = range(type);
                    int height = range(type);
                    Rectangle rect = randomRect(followed, dx, dy, width, height);
                    if (placeRectRoom(rect)) {
                        controlRooms.put(rect, new ControlRoom(cp, room, rect));
                        return rect;
                    }
                }
            }

            count++;
            if (count % 10 == 0) {
                // widen the placement area to allow for more spacing if we can't find anything
                // within the desired area
                dx++;
                dy++;
            }
        }

        // TODO: get first available
        // throw new IllegalStateException("Unable to place: " + cp.getId());
        return place(cp);
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
                Preconditions.checkArgument(
                        keys.containsKey(node.getLock()),
                        String.format("No key for lock %s, encounter %s", node.getLock(),
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

        public Optional<Encounter> chooseEncounter(Collection<Encounter> encounters,
                ConnectedRoomManager rooms) {
            // find all the available encounters for the given control point
            double total = 0;
            List<Encounter> available = new ArrayList<>();
            for (Encounter encounter : encounters) {
                if (matchesFaction(encounter, rooms)) {
                    if (encounter.getControlPointIdList().contains(cp.getId())) {
                        total += encounter.getWeight();
                        available.add(encounter);
                    }
                }
            }

            // System.out.println("choosing for " + cp.getId());
            // System.out.println("available: " + available.size());

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

        public boolean matchesFaction(Encounter encounter, ConnectedRoomManager rooms) {
            Optional<String> controller = rooms.getConnected(this).getFaction();
            if (Strings.isNullOrEmpty(encounter.getFactionId()) && !controller.isPresent()) {
                // no required faction control
                return true;
            }

            if (controller.isPresent()) {
                // compare existing factions
                return encounter.getFactionId().equals(controller.get());
            }

            // no faction claims this room, so we can't match it
            return false;
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

    public static RoomGenerator from(List<ControlPoint> points, long seed) {
        Random rand = new Random(seed);
        int total = 0;
        List<Pair<ControlPoint, Integer>> roomCounts = new ArrayList<>();
        for (ControlPoint cp : points) {
            int count = (int) (rand.nextDouble() * (cp.getMax() - cp.getMin())) + cp.getMin();
            if (count > 0) {
                roomCounts.add(Pair.of(cp, count));
                total += count;
            }
            InvokenGame.log(String.format("%s [%d, %d] -> %d", cp.getId(), cp.getMin(),
                    cp.getMax(), count));
        }
        return new RoomGenerator(total, points, roomCounts, seed);
    }
}
