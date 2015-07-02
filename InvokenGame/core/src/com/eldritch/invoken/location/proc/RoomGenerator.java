package com.eldritch.invoken.location.proc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
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
import com.eldritch.invoken.location.NaturalVector2;
import com.eldritch.invoken.location.proc.RoomDecorator.RoomType;
import com.eldritch.invoken.proto.Locations.ControlPoint;
import com.eldritch.invoken.proto.Locations.Encounter;
import com.eldritch.invoken.proto.Locations.Room;
import com.eldritch.invoken.proto.Locations.Territory;
import com.eldritch.invoken.util.Pair;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class RoomGenerator extends BspGenerator {
    private final RoomCache roomCache = new RoomCache();
    private final List<Pair<ControlPoint, Integer>> roomCounts = new ArrayList<>();
    private final Map<Rectangle, ControlRoom> controlRooms = new LinkedHashMap<>();
    private final Map<String, List<ControlPoint>> follows = new HashMap<>();
    private final Map<String, Compound> compounds = new HashMap<>();
    private final Compound[][] compoundIndex;
    private final CompoundPair[][] compoundPairs;

    public RoomGenerator(int roomCount, List<Territory> territories, List<ControlPoint> points,
            List<Pair<ControlPoint, Integer>> roomCounts, long seed) {
        super(roomCount, seed);
        this.roomCounts.addAll(roomCounts);

        // create compounds
        this.compoundIndex = new Compound[getWidth()][getHeight()];
        for (Territory territory : territories) {
            if (!territory.getCompound()) {
                continue;
            }

            // allocate a rectangular region of the space for the compound, based on the amount of
            // control required
            int size = getSize(territory.getControl(), 75);
            Rectangle bounds = new Rectangle( //
                    range(0, getWidth() - size), //
                    range(0, getHeight() - size), //
                    size, size);
            InvokenGame.logfmt("territory %s at %s", territory.getFactionId(), bounds);

            Compound compound = new Compound(territory, bounds);
            compounds.put(territory.getFactionId(), compound);

            // add to index
            int startX = (int) bounds.x;
            int endX = (int) (bounds.x + bounds.width - 1);
            int startY = (int) bounds.y;
            int endY = (int) (bounds.y + bounds.height - 1);

            for (int x = startX; x <= endX; x++) {
                for (int y = startY; y <= endY; y++) {
                    this.compoundIndex[x][y] = compound;
                }
            }
        }
        this.compoundPairs = new CompoundPair[getWidth()][getHeight()];

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

    public Room getRoom(String id) {
        return roomCache.lookupRoom(id);
    }

    public boolean hasCompound(Territory territory) {
        return compounds.containsKey(territory.getFactionId());
    }

    public Compound getCompound(Territory territory) {
        return compounds.get(territory.getFactionId());
    }

    public List<ControlRoom> getControlRooms(Compound compound) {
        List<ControlRoom> rooms = new ArrayList<>();
        for (Rectangle rect : compound.rooms) {
            rooms.add(controlRooms.get(rect));
        }
        return rooms;
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
                String fid1 = o1.first.getFactionId();
                String fid2 = o2.first.getFactionId();
                if (!Strings.isNullOrEmpty(fid1)) {
                    if (!Strings.isNullOrEmpty(fid2)) {
                        if (fid1.equals(fid2)) {
                            if (o1.first.getAccess() != o2.first.getAccess()) {
                                // sort by access point
                                return o1.first.getAccess() ? -1 : 1;
                            }
                        } else {
                            // sort by faction ID
                            fid1.compareTo(fid2);
                        }
                    }

                    // explicit factions come first
                    return -1;
                } else if (!Strings.isNullOrEmpty(fid2)) {
                    return 1;
                }

                int count1 = follows.containsKey(o1.first.getId()) ? follows.get(o1.first.getId())
                        .size() : 0;
                int count2 = follows.containsKey(o2.first.getId()) ? follows.get(o2.first.getId())
                        .size() : 0;
                return Integer.compare(count2, count1);
            }
        });

        InvokenGame.log("Room Count: " + getRoomCount());
        CostMatrix cost = new DefaultCostMatrix();
        LinkedList<Compound> unbuilt = new LinkedList<>(compounds.values());
        for (Pair<ControlPoint, Integer> elem : roomCounts) {
            ControlPoint cp = elem.first;
            int count = elem.second;
            for (int i = 0; i < count; i++) {
                System.out.println("placing: " + cp.getId());
                place(cp, cost, unbuilt);
            }
        }
    }

    private Rectangle place(ControlPoint cp, CostMatrix cost, LinkedList<Compound> unbuilt) {
        if (!Strings.isNullOrEmpty(cp.getFactionId()) && compounds.containsKey(cp.getFactionId())
                && compounds.get(cp.getFactionId()).hasRemainingControl()) {
            // this room must be placed within this compound
            Compound compound = compounds.get(cp.getFactionId());
            Rectangle room = place(cp, cost, compound.getBounds());
            compound.addRoom(room);
            controlRooms.get(room).setTerritory(compound.territory);
            return room;
        } else if (cp.getValue() > 0 && Strings.isNullOrEmpty(cp.getFactionId())) {
            // this point is eligible for placing into a specific compound
            Compound next = getNext(unbuilt);
            if (next != null) {
                // place the room in this compound
                Rectangle room = place(cp, cost, next.getBounds());
                next.addRoom(room);
                controlRooms.get(room).setTerritory(next.territory);
                return room;
            }
        }

        // no value or compound, so place normally
        return place(cp, cost, getBounds());
    }

    private Compound getNext(LinkedList<Compound> unbuilt) {
        while (!unbuilt.isEmpty()) {
            Compound next = unbuilt.element();
            if (!next.hasRemainingControl()) {
                unbuilt.remove();
            } else {
                return next;
            }
        }
        return null;
    }

    private ControlNode sample(ControlNode current, List<ControlNode> sample) {
        return sample.get((int) (random() * sample.size()));

        // ControlNode closest = null;
        // int bestDistance = Integer.MAX_VALUE;
        // for (ControlNode node : sample) {
        // int distance = (int) (Math.abs(current.getBounds().x - node.getBounds().x) + Math
        // .abs(current.getBounds().y - node.getBounds().y));
        // if (distance < bestDistance) {
        // closest = node;
        // bestDistance = distance;
        // }
        // }
        // return closest;
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

        // draw from a separate well for connecting rooms in compounds
        Map<String, List<ControlNode>> compoundSample = new HashMap<>();
        for (Compound compound : compounds.values()) {
            compoundSample.put(compound.territory.getFactionId(), new ArrayList<ControlNode>());
        }

        // seed the routine so we can connect to the origin, and we connected from a child
        connections++;
        connected.add(origin);
        if (!origin.cp.getClosed()) {
            connectedSample.add(origin);
        }
        for (ControlNode lock : origin.locks) {
            unlocked.add(lock);
        }

        CostMatrix costs = new EncounterCostMatrix(getWidth(), getHeight(), controlRooms.values(),
                compoundIndex);
        while (!unlocked.isEmpty()) {
            ControlNode current = unlocked.removeFirst();
            if (connected.contains(current)) {
                // already placed
                continue;
            }

            connections++;
            connected.add(current);
            if (!current.cp.getClosed()) {
                if (current.controlRoom.hasTerritory()) {
                    // we use separate wells for connecting compound rooms
                    List<ControlNode> sample = compoundSample.get(current.controlRoom
                            .getTerritory().getFactionId());
                    if (!sample.isEmpty()) {
                        ControlNode connection = sample(current, sample);
                        DigTunnel(connection.getBounds(), current.getBounds(), costs);
                    }

                    sample.add(current);
                }

                if (!current.controlRoom.hasTerritory() || current.cp.getAccess()) {
                    // can connect implicitly
                    if (!connectedSample.isEmpty()) {
                        ControlNode connection = sample(current, connectedSample);
                        DigTunnel(connection.getBounds(), current.getBounds(), costs);
                        if (connection.cp.getAccess()) {
                            connectedSample.remove(connection);
                        }
                    }

                    // add this node to the connected set, and maybe add its children if all its
                    // keys
                    // are also in the connected set
                    if (!current.cp.getAccess() || connectedSample.isEmpty()) {
                        connectedSample.add(current);
                    }
                }
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

    private class EncounterCostMatrix implements CostMatrix {
        private final ControlRoom[][] rooms;
        private final Compound[][] compounds;

        public EncounterCostMatrix(int width, int height, Collection<ControlRoom> list,
                Compound[][] compoundIndex) {
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
            this.compounds = compoundIndex;
        }

        @Override
        public int getCost(int x1, int y1, int x2, int y2) {
            ControlRoom room = rooms[x2][y2];
            int cost = 0;
            if (room != null) {
                cost += getCost(room);
                cost += 100 * room.getCenterXDistance(x2);
                cost += 100 * room.getCenterYDistance(y2);
            }
            if (compounds[x1][y1] != compounds[x2][y2]) {
                // heavy penalty for crossing territory
                cost *= 10;
                cost += 1000;

                if (compoundPairs[x2][y2] != null) {
                    CompoundPair cp = compoundPairs[x2][y2];
                    if (cp.c1 != compounds[x1][y1] || cp.c2 != compounds[x2][y2]) {
                        // crossing a hall that connects different territory types, which we really
                        // want to avoid
                        cost += 2000;
                    }
                }

                // when crossing territory, it's actually more expensive to touch floor tiles
                if (compounds[x2][y2] != null && getType(x2, y2) != CellType.Wall) {
                    cost += 500;
                }
            }
            return cost;
        }

        private int getCost(ControlRoom room) {
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

    @Override
    protected void addPath(int x, int y, int x2, int y2, List<NaturalVector2> path) {
        super.addPath(x, y, x2, y2, path);
        for (NaturalVector2 point : path) {
            compoundPairs[point.x][point.y] = new CompoundPair(x, y, x2, y2);
        }
    }

    private class CompoundPair {
        final Compound c1;
        final Compound c2;

        public CompoundPair(int x1, int y1, int x2, int y2) {
            this(compoundIndex[x1][y1], compoundIndex[x2][y2]);
        }

        public CompoundPair(Compound c1, Compound c2) {
            this.c1 = c1;
            this.c2 = c2;
        }
    }

    private Rectangle place(ControlPoint cp, CostMatrix cost, Rectangle bounds) {
        // first, place the room somewhere valid on the map
        Rectangle rect = place(cp, bounds);

        // next, place and connected to our followers
        placeFollowers(cp, rect, cost, bounds);
        return rect;
    }

    private void placeFollowers(ControlPoint cp, Rectangle origin, CostMatrix cost, Rectangle bounds) {
        if (follows.containsKey(cp.getId())) {
            for (ControlPoint follower : follows.get(cp.getId())) {
                Rectangle rect = placeFollower(follower, origin, bounds);
                DigTunnel(origin, rect, cost);
                InvokenGame.log(cp.getId() + " -> " + follower.getId());
                placeFollowers(follower, rect, cost, bounds);
            }
        }
    }

    // private Rectangle placeFollowers(ControlPoint cp, Rectangle origin, CostMatrix cost,
    // Rectangle bounds) {
    // Rectangle rect = origin == null ? place(cp, bounds) : placeFollower(cp, origin, bounds);
    //
    // // following
    // if (follows.containsKey(cp.getId())) {
    // for (ControlPoint follower : follows.get(cp.getId())) {
    // Rectangle rect2 = placeFollowers(follower, rect, cost, bounds);
    // // Rectangle rect2 = place(follower, rect);
    // DigTunnel(rect, rect2, cost);
    // // System.out.println(rect + " -> " + rect2);
    // InvokenGame.log(cp.getId() + " -> " + follower.getId());
    // }
    // }
    //
    // return rect;
    // }

    private Rectangle place(ControlPoint cp, Rectangle bounds) {
        // InvokenGame.log("Place: " + encounter.getId());
        int count = 0;
        while (count < 1000) {
            for (Room room : getRooms(cp)) {
                RoomType type = RoomDecorator.get(room.getSize());

                int width = range(type);
                int height = range(type);
                Rectangle rect = getRectangle(bounds, width, height);
                if (canPlace(rect, bounds)) {
                    place(rect);
                    controlRooms.put(rect, new ControlRoom(cp, room, rect));
                    return rect;
                }
            }
            count++;
        }

        // TODO: get first available
        throw new IllegalStateException("Unable to place: " + cp.getId());
    }

    private List<Room> getRooms(ControlPoint cp) {
        List<Room> results = new ArrayList<>();
        if (!cp.getRoomIdList().isEmpty()) {
            for (String roomId : cp.getRoomIdList()) {
                results.add(roomCache.lookupRoom(roomId));
            }
        } else {
            results.addAll(InvokenGame.ROOM_SELECTOR.getGenericRooms(getRandom()));
        }
        return results;
    }

    protected boolean canPlace(Rectangle rect, Rectangle bounds) {
        if (canPlace(rect)) {
            int startX = (int) rect.x;
            int startY = (int) rect.y;
            if (!isClear(startX, startY, bounds)) {
                return false;
            }

            int endX = (int) (rect.x + rect.width - 1);
            int endY = (int) (rect.y + rect.height - 1);
            if (!isClear(endX, endY, bounds)) {
                return false;
            }

            return true;
        }

        return false;
    }

    private boolean isClear(int x, int y, Rectangle bounds) {
        Compound c = compoundIndex[x][y];
        if (c != null) {
            // reference the same bounds pointer, then they are the same compound
            if (bounds != c.getBounds()) {
                return false;
            }
        }
        return true;
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

    private Rectangle placeFollower(ControlPoint cp, Rectangle followed, Rectangle bounds) {
        // InvokenGame.log("Place: " + encounter.getId());
        int dx = 0;
        int dy = 0;
        int count = 0;
        while (count < 1000) {
            if (cp.getRoomIdList().isEmpty()) {
                int width = range(RoomDecorator.MIN_ROOM_SIZE, RoomDecorator.MAX_ROOM_SIZE);
                int height = range(RoomDecorator.MIN_ROOM_SIZE, RoomDecorator.MAX_ROOM_SIZE);
                Rectangle rect = randomRect(followed, dx, dy, width, height);
                if (canPlace(rect, bounds)) {
                    place(rect);
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
                    if (canPlace(rect, bounds)) {
                        place(rect);
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
        return place(cp, bounds);
    }

    private int range(RoomType type) {
        int min = Math.max(type.getMin(), RoomDecorator.MIN_ROOM_SIZE);
        int max = Math.min(type.getMax(), RoomDecorator.MAX_ROOM_SIZE);
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
        private Optional<Territory> territory = Optional.absent();

        public ControlRoom(ControlPoint cp, Room room, Rectangle bounds) {
            this.cp = cp;
            this.room = room;
            this.bounds = bounds;
        }

        public boolean hasTerritory() {
            return territory.isPresent();
        }

        public Territory getTerritory() {
            return territory.get();
        }

        public void setTerritory(Territory territory) {
            this.territory = Optional.of(territory);
        }

        public ControlPoint getControlPoint() {
            return cp;
        }

        public Room getRoom() {
            return room;
        }
        
        public int getCenterXDistance(int x) {
        	return Math.abs((int) (bounds.x + bounds.width / 2) - x);
        }
        
        public int getCenterYDistance(int y) {
        	return Math.abs((int) (bounds.y + bounds.height / 2) - y);
        }
        
        public boolean isCorner(int x, int y) {
        	return (x == bounds.x || x == (int) (bounds.x + bounds.width - 1))
        			&& (y == bounds.y || y == (int) (bounds.y + bounds.height - 1));
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
        
        private boolean matchesPoint(Encounter encounter, ControlPoint cp) {
            if (encounter.getControlPointIdList().isEmpty()) {
                return !cp.getOrigin();
            } else {
                return encounter.getControlPointIdList().contains(cp.getId());
            }
        }

        public Optional<Encounter> chooseEncounter(Collection<Encounter> encounters,
                ConnectedRoomManager rooms) {
            // find all the available encounters for the given control point
            double total = 0;
            List<Encounter> available = new ArrayList<>();
            for (Encounter encounter : encounters) {
                if (matchesFaction(encounter, rooms)) {
                    if (matchesPoint(encounter, cp)) {
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

    public static class Compound {
        private final Set<Rectangle> rooms = new HashSet<>();
        private final Territory territory;
        private final Rectangle bounds;
        private int control;

        public Compound(Territory territory, Rectangle bounds) {
            this.territory = territory;
            this.bounds = bounds;
            this.control = territory.getControl();
        }

        public Territory getTerritory() {
            return territory;
        }

        public Rectangle getBounds() {
            return bounds;
        }

        public void addRoom(Rectangle room) {
            rooms.add(room);
            control--;
        }

        public boolean hasRemainingControl() {
            return control > 0;
        }
    }

    public static RoomGenerator from(List<Territory> territories, List<ControlPoint> points,
            long seed) {
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
        return new RoomGenerator(total, territories, points, roomCounts, seed);
    }
}
