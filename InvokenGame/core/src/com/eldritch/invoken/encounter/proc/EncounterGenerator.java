package com.eldritch.invoken.encounter.proc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
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
    private final Map<Rectangle, EncounterRoom> encounterRooms = new HashMap<Rectangle, EncounterRoom>();
    private final EncounterNode dependencies;

    public EncounterGenerator(int roomCount, List<Encounter> encounters) {
        super(roomCount);
        this.encounters.addAll(encounters);
        dependencies = generateDependencyGraph(encounters);
    }

    public Collection<EncounterRoom> getEncounterRooms() {
        return encounterRooms.values();
    }

    @Override
    protected void PlaceRooms() {
        InvokenGame.log("Room Count: " + getRoomCount());

        // place all encounters at least once first
        int count = 0;
        for (Encounter encounter : encounters) {
            if (place(encounter)) {
                count++;
            }
        }

        // TODO: only sample from the non-unique encounters and weight them
        List<Encounter> repeatedEncounters = new ArrayList<Encounter>();
        for (Encounter encounter : encounters) {
            if (!encounter.getUnique()) {
                repeatedEncounters.add(encounter);
            }
        }
        EncounterSelector selector = new EncounterSelector(repeatedEncounters);

        // place encounters randomly
        int remaining = getRoomCount() - count;
        InvokenGame.log("Remaining: " + remaining);
        while (remaining > 0) {
            Encounter encounter = selector.select();
            place(encounter);
            remaining--;
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

    public static class EncounterSelector {
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
            search.cumulativeWeight = Math.random() * totalWeight;
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

    private static EncounterNode generateDependencyGraph(List<Encounter> encounters) {
        // scan through the list and pick out the origin
        List<EncounterNode> nodes = new ArrayList<EncounterNode>();
        EncounterNode origin = null;
        for (Encounter encounter : encounters) {
            EncounterNode node = new EncounterNode(encounter);
            if (node.isOrigin()) {
                origin = node;
            }
            nodes.add(node);
        }
        Preconditions.checkState(origin != null, "Origin must be provided in the encounter list");

        // add the origin as a key of all unlocked encounters
        Map<String, List<EncounterNode>> keys = new HashMap<String, List<EncounterNode>>();
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
    }

    private static class EncounterNode {
        // encounters that unlock this one
        private final List<EncounterNode> keys = new ArrayList<EncounterNode>();

        // encounters that are unlocked by this one
        private final List<EncounterNode> locks = new ArrayList<EncounterNode>();

        private final Encounter encounter;

        public EncounterNode(Encounter encounter) {
            this.encounter = encounter;
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
    }
}
