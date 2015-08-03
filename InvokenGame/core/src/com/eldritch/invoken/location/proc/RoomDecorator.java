package com.eldritch.invoken.location.proc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.math.Rectangle;
import com.eldritch.invoken.location.ConnectedRoom;
import com.eldritch.invoken.location.ConnectedRoomManager;
import com.eldritch.invoken.location.NaturalVector2;
import com.eldritch.invoken.location.layer.LocationLayer.CollisionLayer;
import com.eldritch.invoken.location.layer.LocationMap;
import com.eldritch.invoken.location.proc.FurnitureLoader.PlaceableFurniture;
import com.eldritch.invoken.location.proc.RoomGenerator.ControlRoom;
import com.eldritch.invoken.proto.Locations.Furniture;
import com.eldritch.invoken.proto.Locations.Room;

public class RoomDecorator {
    // threshold of furniture to open ground in room, past which we need to stop
    // adding furniture
    public static final double MAX_FURNITURE = 0.5;

    public static final int MIN_ROOM_SIZE = 8;
    public static final int MAX_ROOM_SIZE = 20;

    enum RoomType {
        SMALL(0, 10), MEDIUM(11, 15), LARGE(16, Integer.MAX_VALUE);

        private final int min;
        private final int max;

        private RoomType(int min, int max) {
            this.min = min;
            this.max = max;
        }

        public int getMin() {
            return min;
        }

        public int getMax() {
            return max;
        }
    }

    private static final EnumMap<Room.Size, RoomType> sizeToType = new EnumMap<Room.Size, RoomType>(
            Room.Size.class);

    static {
        sizeToType.put(Room.Size.SMALL, RoomType.SMALL);
        sizeToType.put(Room.Size.MEDIUM, RoomType.MEDIUM);
        sizeToType.put(Room.Size.LARGE, RoomType.LARGE);
    }

    private final LocationMap map;
    private final Random rand;
    private final LootGenerator lootGenerator;

    public RoomDecorator(int floor, LocationMap map, long seed) {
        this.map = map;
        this.rand = new Random(seed);
        this.lootGenerator = new LootGenerator(floor);
    }

    public void generate(ConnectedRoomManager rooms, List<Room> halls) {
        // chambers
        for (Entry<ControlRoom, ConnectedRoom> room : rooms.getChambers()) {
            place(room.getKey(), room.getValue());
        }

        // halls
        if (!halls.isEmpty()) {
            for (ConnectedRoom connected : rooms.getRooms()) {
                if (!connected.isChamber()) {
                    place(halls.get((int) (Math.random() * halls.size())), connected);
                }
            }
        }

        // remove collision points used during procedural generation
        for (MapLayer layer : map.getLayers()) {
            if (layer instanceof CollisionLayer) {
                CollisionLayer collision = (CollisionLayer) layer;
                collision.removeTransient();
            }
        }
    }
    
    public void place(List<Furniture> furnitureList, ConnectedRoom connected) {
        place(furnitureList, connected, connected.getAllPoints().size());

        // remove collision points used during procedural generation
        for (MapLayer layer : map.getLayers()) {
            if (layer instanceof CollisionLayer) {
                CollisionLayer collision = (CollisionLayer) layer;
                collision.removeTransient();
            }
        }
    }

    private void place(ControlRoom encounter, ConnectedRoom connected) {
        // InvokenGame.log("placing: " + encounter.getEncounter().getId());
        Room room = encounter.getRoom();

        // decrease bounds by 1 in each direction to prevent placing on border
        Rectangle bounds = encounter.getRestrictedBounds();
        double area = bounds.area();

        place(room, connected, area);
    }

    private void place(Room room, ConnectedRoom connected) {
        place(room, connected, connected.getAllPoints().size());
    }

    private void place(Room room, ConnectedRoom connected, double area) {
        place(room.getFurnitureList(), connected, area);
    }

    private void place(List<Furniture> furnitureList, ConnectedRoom connected, double area) {
        int coveredTiles = 0; // running count of covered tiles
        List<Furniture> availableFurniture = new ArrayList<Furniture>(furnitureList);

        Collections.shuffle(availableFurniture, rand);
        Collections.sort(availableFurniture, new Comparator<Furniture>() {
            @Override
            public int compare(Furniture f1, Furniture f2) {
                // descending by the number of required furniture pieces, otherwise stable in the
                // shuffled ordering
                return Integer.compare(f2.getMin(), f1.getMin());
            }
        });

        for (Furniture furniture : availableFurniture) {
            int placed = 0;
            for (int i = 0; i < furniture.getMax(); i++) {
                // calculate the percentage of furniture coverage to ground
                // tiles adding this piece of furniture would cost us
                PlaceableFurniture placeable = FurnitureLoader.load(furniture, this);
                int cost = placeable.getCost();
                double coverage = (coveredTiles + cost) / area;

                // find a suitable place in room that satisfies the constraints
                if (coverage < MAX_FURNITURE) {
                    NaturalVector2 position = placeable.findPosition(connected, map, rand);
                    if (position != null) {
                        // found a place to put the furniture, so merge it into
                        // the map
                        placeable.place(position, map);
                        coveredTiles += cost;
                        placed++;
                    }
                }
            }

            // some furniture is required for the room to be valid
            if (furniture.hasMin() && placed < furniture.getMin()) {
                throw new IllegalStateException(String.format(
                        "Room decorator placed: %d of furniture %s, required: %d", placed,
                        furniture.getId(), furniture.getMin()));
            }
        }
    }
    
    public LootGenerator getLootGenerator() {
        return lootGenerator;
    }

    public static RoomType get(Room.Size size) {
        return sizeToType.get(size);
    }
}
