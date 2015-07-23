package com.eldritch.invoken.location.proc;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.activators.Activator;
import com.eldritch.invoken.activators.DoorActivator;
import com.eldritch.invoken.activators.util.LockManager.LockInfo;
import com.eldritch.invoken.gfx.Light;
import com.eldritch.invoken.gfx.Light.StaticLight;
import com.eldritch.invoken.location.ConnectedRoom;
import com.eldritch.invoken.location.ConnectedRoomManager;
import com.eldritch.invoken.location.NaturalVector2;
import com.eldritch.invoken.location.layer.LocationCell;
import com.eldritch.invoken.location.layer.LocationLayer;
import com.eldritch.invoken.location.layer.LocationLayer.CollisionLayer;
import com.eldritch.invoken.location.layer.LocationMap;
import com.eldritch.invoken.location.layer.RemovableCell;
import com.eldritch.invoken.location.proc.RoomGenerator.ControlRoom;
import com.eldritch.invoken.proto.Locations.ControlPoint;

public abstract class FurnitureGenerator {
    private final Random rand;
    private final Set<NaturalVector2> marked = new LinkedHashSet<NaturalVector2>();
    private final TextureAtlas atlas;
    protected final TiledMapTile ground;

    public FurnitureGenerator(TextureAtlas atlas, TiledMapTile ground, long seed) {
        this.rand = new Random(seed);
        this.atlas = atlas;
        this.ground = ground;
    }

    public void addLights(LocationLayer layer, LocationLayer base, List<Light> lights,
            TiledMapTile placer) {
        TiledMapTile light = new StaticTiledMapTile(atlas.findRegion("future/light1"));
        for (int y = 0; y < base.getHeight(); y++) {
            // scan by row so we can properly distribute lights
            int lastLight = 0;
            for (int x = 0; x < base.getWidth(); x++) {
                Cell cell = base.getCell(x, y);
                if (cell != null && cell.getTile() == placer && !marked(x, y)) {
                    // with some probability, add a light to the wall
                    if (lastLight == 1 && rand.nextDouble() < 0.75) {
                        addCell(layer, light, x, y);
                        lights.add(new StaticLight(new Vector2(x + 0.5f, y)));
                    }
                    lastLight = (lastLight + 1) % 5;
                } else {
                    // distribute along consecutive walls, so reset when there's a gap
                    lastLight = 0;
                }
            }
        }
    }

    public void createDoors(ConnectedRoomManager rooms, LocationLayer base,
            CollisionLayer collision, StaticTiledMapTile collider, List<Activator> activators) {
        for (Entry<ControlRoom, ConnectedRoom> room : rooms.getChambers()) {
            ControlPoint metadata = room.getKey().getControlPoint();
            ConnectedRoom connectedRoom = room.getValue();
            for (NaturalVector2 point : room.getValue().getAllPoints()) {
                int x = point.x;
                int y = point.y;

                if (marked(x, y)) {
                    // already something here
                    continue;
                }

                if (base.isGround(x, y) && base.isGround(x + 1, y) && isLowerGap(x, y, base)) {
                    DoorActivator activator = DoorActivator.createFront(x, y,
                            LockInfo.from(metadata, connectedRoom));
                    activators.add(activator);
                    mark(x, y);
                } else if (base.isGround(x, y - 1) && base.isGround(x + 1, y - 1)
                        && isUpperGap(x, y - 1, base)) {
                    DoorActivator activator = DoorActivator.createFront(x, y - 1,
                            LockInfo.from(metadata, connectedRoom));
                    activators.add(activator);
                    mark(x, y - 1);
                }
            }

            for (NaturalVector2 point : room.getValue().getPoints()) {
                int x = point.x;
                int y = point.y;

                if (marked(x, y)) {
                    // already something here
                    continue;
                }

                if (base.isGround(x, y - 1) && isSideGap(x, y - 1, base)) {
                    DoorActivator activator = DoorActivator.createSide(x, y,
                            LockInfo.from(metadata, connectedRoom));
                    activators.add(activator);
                    mark(x, y);
                }
            }
        }

        // add collision tiles for all marked positions
        TiledMapTile tempTile = new StaticTiledMapTile(collider);
        tempTile.getProperties().put("transient", "");
        for (NaturalVector2 point : marked) {
            collision.addCell(tempTile, point.x, point.y);

            // TODO: add tiles to the buffer area around the marked tiles
        }
    }

    private boolean isLowerGap(int x, int y, LocationLayer base) {
        return !base.isGround(x - 1, y) && !base.isGround(x + 2, y) // sides are walls
                && !base.isGround(x - 1, y + 1) && !base.isGround(x + 2, y + 1) // walls above
                && (base.isGround(x - 1, y - 1) || base.isGround(x + 2, y - 1)); // ground below
    }

    private boolean isUpperGap(int x, int y, LocationLayer base) {
        return !base.isGround(x - 1, y) && !base.isGround(x + 2, y) // sides are walls
                && !base.isGround(x - 1, y - 1) && !base.isGround(x + 2, y - 1) // walls below
                && (base.isGround(x - 1, y + 1) || base.isGround(x + 2, y + 1)); // ground above
    }

    private boolean isSideGap(int x, int y, LocationLayer base) {
        return !base.isGround(x, y + 2) && !base.isGround(x, y - 1) // walls above and below
                && (base.isGround(x - 1, y + 1) || base.isGround(x + 1, y + 1)); // ground at side
    }

    private void mark(int x, int y) {
        marked.add(NaturalVector2.of(x, y));
    }

    public boolean marked(int x, int y) {
        return marked.contains(NaturalVector2.of(x, y));
    }

    public Cell addCell(LocationLayer layer, TiledMapTile tile, int x, int y) {
        Cell cell = new LocationCell(NaturalVector2.of(x, y), layer);
        cell.setTile(tile);
        layer.setCell(x, y, cell);
        mark(x, y);
        return cell;
    }

    public void addCell(LocationLayer layer, TiledMapTile tile, int x, int y,
            List<RemovableCell> cells) {
        Cell cell = addCell(layer, tile, x, y);
        cells.add(new RemovableCell(cell, layer, x, y));
        mark(x, y);
    }

    public abstract LocationLayer generateClutter(LocationLayer base, LocationMap map);
}
