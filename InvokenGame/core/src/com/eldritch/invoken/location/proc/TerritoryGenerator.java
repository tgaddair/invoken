package com.eldritch.invoken.location.proc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Map.Entry;

import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.location.ConnectedRoom;
import com.eldritch.invoken.location.ConnectedRoomManager;
import com.eldritch.invoken.location.proc.RoomGenerator.Compound;
import com.eldritch.invoken.location.proc.RoomGenerator.ControlRoom;
import com.eldritch.invoken.proto.Locations.Territory;

public class TerritoryGenerator {
    private final RoomGenerator generator;
    private final ConnectedRoomManager rooms;
    private final List<Territory> territories;

    public TerritoryGenerator(RoomGenerator generator, ConnectedRoomManager rooms,
            List<Territory> territories) {
        this.generator = generator;
        this.rooms = rooms;
        this.territories = territories;
    }

    public void claim() {
        // define a number of sectors to roughly divide the territories on opposite ends of the map
        // in general, we want to avoid placing capitals right next to one another so that one
        // territory gets immediately surrounded and subsequently swarmed by another
        int sectors = (int) Math.ceil(Math.sqrt(territories.size()));
        InvokenGame.logfmt("Sectors: %d", sectors);

        // randomly assign capitals for every faction with territory
        int sectorX = 0;
        int sectorY = 0;
        Map<ConnectedRoom, GrowthRegion> claimed = new HashMap<>();
        List<GrowthRegion> regions = new ArrayList<>();
        for (Territory territory : territories) {
            if (generator.hasCompound(territory)) {
                GrowthRegion region = new GrowthRegion(territory, claimed);
                Compound compound = generator.getCompound(territory);
                List<ControlRoom> controlRooms = generator.getControlRooms(compound);

                // claim chambers
                Set<ConnectedRoom> owned = new HashSet<>();
                for (ControlRoom cr : controlRooms) {
                    ConnectedRoom room = rooms.getConnected(cr);
                    room.setFaction(territory.getFactionId());
                    claimed.put(room, region);
                    owned.add(room);
                }

                // claim hallways
                for (ConnectedRoom room : owned) {
                    for (ConnectedRoom neighbor : room.getNeighbors()) {
                        if (!neighbor.isChamber() && allClaimed(neighbor, owned)) {
                            neighbor.setFaction(territory.getFactionId());
                            claimed.put(neighbor, region);
                        }
                    }
                }
            } else {
                // choose a random point in the sector, find the nearest unclaimed room to act as
                // the capital
                InvokenGame.logfmt("Placing at sector (%d,  %d)", sectorX, sectorY);

                // only assign a capital of the faction has some remaining control in the area
                int control = territory.getControl();
                if (control > 0) {
                    // choose a room with the greatest number of connections
                    ConnectedRoom capital = findCapital(territory.getFactionId(), rooms, claimed);
                    if (capital == null) {
                        // something went wrong
                        throw new IllegalStateException("Failed to find capital");
                    }

                    // claim the capital
                    // grow territory outwards from each capital until all control is expended
                    InvokenGame.logfmt("Claiming %s as capital for %s", capital.getCenter(),
                            territory.getFactionId());
                    regions.add(new GrowthRegion(territory, capital, claimed, rooms));

                    // update sectors
                    sectorX++;
                    if (sectorX >= sectors) {
                        sectorX = 0;
                        sectorY++;
                    }
                }
            }
        }

        // grow each region in turns to prevent starving out a region
        boolean canGrow = true;
        while (canGrow) {
            canGrow = false;
            for (GrowthRegion region : regions) {
                if (region.canGrow()) {
                    region.grow();
                    canGrow = true;
                }
            }
        }
    }

    private boolean allClaimed(ConnectedRoom room, Set<ConnectedRoom> claimed) {
        for (ConnectedRoom neighbor : room.getNeighbors()) {
            // if (room != neighbor && claimed.contains(neighbor)) {
            if (neighbor.isChamber() && !claimed.contains(neighbor)) {
                return false;
            }
        }
        return true;
    }

    private static ConnectedRoom findCapital(String factionId, ConnectedRoomManager rooms,
            Map<ConnectedRoom, GrowthRegion> claimed) {
        // first try and choose a high value room that defaults to the current faction
        int bestValue = Integer.MIN_VALUE;
        ConnectedRoom capital = null;
        for (Entry<ControlRoom, ConnectedRoom> chamber : rooms.getChambers()) {
            ControlRoom cr = chamber.getKey();
            if (cr.hasTerritory() && !cr.getTerritory().getFactionId().equals(factionId)) {
                // owned by another faction
                continue;
            }

            ConnectedRoom room = chamber.getValue();
            int value = cr.getControlPoint().getValue();
            if (!claimed.containsKey(room) && factionId.equals(cr.getControlPoint().getFactionId())
                    && value > bestValue) {
                // found default point
                InvokenGame
                        .logfmt("Found point %s for %s", cr.getControlPoint().getId(), factionId);
                bestValue = value;
                capital = room;
            }
        }

        // fallback plan: choose capital based on which unclaimed point has the most out-links
        if (capital == null) {
            int maxConnections = 0;
            for (Entry<ControlRoom, ConnectedRoom> chamber : rooms.getChambers()) {
                ControlRoom cr = chamber.getKey();
                if (cr.hasTerritory() && !cr.getTerritory().getFactionId().equals(factionId)) {
                    // owned by another faction
                    continue;
                }

                ConnectedRoom room = chamber.getValue();
                if (!claimed.containsKey(room) && cr.getControlPoint().getValue() > 0) {
                    int connections = room.getNeighbors().size();
                    if (connections > maxConnections) {
                        maxConnections = connections;
                        capital = room;
                    }
                }
            }
        }

        return capital;
    }

    private static class GrowthRegion {
        private final Territory territory;
        private final ConnectedRoomManager rooms;
        private final PriorityQueue<ConnectedRoom> bestRooms;
        private final PriorityQueue<ConnectedRoom> reclaimed;
        private final Map<ConnectedRoom, GrowthRegion> claimed;
        private final Set<ConnectedRoom> visited = new HashSet<>();

        private boolean allClaimed = false;
        private int control;

        public GrowthRegion(Territory territory, Map<ConnectedRoom, GrowthRegion> claimed) {
            this.territory = territory;
            this.rooms = null;
            this.bestRooms = null;
            this.reclaimed = null;
            this.claimed = claimed;
        }

        public GrowthRegion(Territory territory, ConnectedRoom capital,
                Map<ConnectedRoom, GrowthRegion> claimed, ConnectedRoomManager rooms) {
            this.territory = territory;
            this.rooms = rooms;
            this.bestRooms = createQueue(rooms);
            this.reclaimed = createQueue(rooms);

            // shared between regions
            this.claimed = claimed;
            claimed.put(capital, this);
            capital.setFaction(territory.getFactionId());

            // start with one less control, because we already claimed a capital
            this.control = territory.getControl() - 1;

            // avoid visiting the same room more than once
            // diallow visiting any rooms that are already claimed
            visited.add(capital);

            // seed the queue with the neighbors of the capital
            addNeighbors(capital);
        }

        public boolean canGrow() {
            // return control > 0 && !(bestRooms.isEmpty() && reclaimed.isEmpty());
            return control > 0 && (!bestRooms.isEmpty() || !allClaimed);
        }

        public void grow() {
            if (bestRooms.isEmpty()) {
                ConnectedRoom capital = findCapital(territory.getFactionId(), rooms, claimed);
                if (capital != null) {
                    // reseed with the new capital
                    InvokenGame.log("reseeding");
                    claim(capital);
                } else {
                    // everything we can grab has already been claimed, so give up
                    allClaimed = true;
                }
            }

            if (!bestRooms.isEmpty()) {
                // claim the next room
                ConnectedRoom room = bestRooms.remove();
                if (!claimed.containsKey(room)) {
                    claim(room);
                }
            }
        }

        private void claim(ConnectedRoom room) {
            claimed.put(room, this);
            room.setFaction(territory.getFactionId());
            addNeighbors(room);
            if (room.isChamber()) {
                control--;
            }
        }

        private void addNeighbors(ConnectedRoom room) {
            for (ConnectedRoom neighbor : room.getNeighbors()) {
                if (!visited.contains(neighbor)) {
                    if (claimed.containsKey(neighbor)) {
                        reclaimed.add(neighbor);
                    } else if (!neighbor.isChamber()
                            || rooms.getControlRoom(neighbor).getControlPoint().getValue() > 0) {
                        bestRooms.add(neighbor);
                    }
                    visited.add(neighbor);
                }
            }
        }
    }

    private static PriorityQueue<ConnectedRoom> createQueue(final ConnectedRoomManager rooms) {
        return new PriorityQueue<ConnectedRoom>(1, new Comparator<ConnectedRoom>() {
            @Override
            public int compare(ConnectedRoom r1, ConnectedRoom r2) {
                // the priority queue is a min heap, so we need to invert the comparison
                return Integer.compare(getValue(r2), getValue(r1));
            }

            private int getValue(ConnectedRoom room) {
                // hallways are free, chambers have an assigned value
                return rooms.hasEncounter(room) ? rooms.getControlRoom(room).getControlPoint()
                        .getValue() : 0;
            }
        });
    }
}
