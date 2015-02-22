package com.eldritch.invoken.encounter.proc;

import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.activators.Activator;
import com.eldritch.invoken.activators.DoorActivator;
import com.eldritch.invoken.activators.DoorActivator.LockInfo;
import com.eldritch.invoken.encounter.ConnectedRoom;
import com.eldritch.invoken.encounter.ConnectedRoomManager;
import com.eldritch.invoken.encounter.NaturalVector2;
import com.eldritch.invoken.encounter.layer.LocationCell;
import com.eldritch.invoken.encounter.layer.LocationLayer;
import com.eldritch.invoken.encounter.layer.LocationMap;
import com.eldritch.invoken.encounter.layer.RemovableCell;
import com.eldritch.invoken.encounter.proc.EncounterGenerator.EncounterRoom;
import com.eldritch.invoken.gfx.Light;
import com.eldritch.invoken.gfx.Light.StaticLight;
import com.eldritch.invoken.proto.Locations.Encounter;
import com.eldritch.invoken.util.Settings;

public abstract class FurnitureGenerator {
    private final Set<NaturalVector2> marked = new HashSet<NaturalVector2>();
    private final TextureAtlas atlas;
    protected final TiledMapTile ground;

    public FurnitureGenerator(TextureAtlas atlas, TiledMapTile ground) {
        this.atlas = atlas;
        this.ground = ground;
    }

    public void addLights(LocationLayer layer, LocationLayer base, List<Light> lights,
            TiledMapTile placer) {
        TiledMapTile light = new StaticTiledMapTile(atlas.findRegion("test-biome/light1"));
        light.setOffsetY(-Settings.PX / 2);
        for (int y = 0; y < base.getHeight(); y++) {
            // scan by row so we can properly distribute lights
            int lastLight = 0;
            for (int x = 0; x < base.getWidth(); x++) {
                Cell cell = base.getCell(x, y);
                if (cell != null && cell.getTile() == placer && !marked(x, y)) {
                    // with some probability, add a light to the wall
                    if (lastLight == 1 && Math.random() < 0.75) {
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
            List<Activator> activators) {
        for (Entry<EncounterRoom, ConnectedRoom> room : rooms.getChambers()) {
            Encounter metadata = room.getKey().getEncounter();
            for (NaturalVector2 point : room.getValue().getPoints()) {
                int x = point.x;
                int y = point.y;

                if (marked(x, y)) {
                    // already something here
                    continue;
                }

                if (base.isGround(x, y) && base.isGround(x + 1, y) && isLowerGap(x, y, base)) {
                    DoorActivator activator = DoorActivator.createFront(x, y,
                            LockInfo.from(metadata));
                    activators.add(activator);
                    mark(x, y);
                } else if (base.isGround(x, y - 1) && base.isGround(x + 1, y - 1)
                        && isUpperGap(x, y - 1, base)) {
                    DoorActivator activator = DoorActivator.createFront(x, y - 1,
                            LockInfo.from(metadata));
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

                if (base.isGround(x, y) && isSideGap(x, y, base)) {
                    DoorActivator activator = DoorActivator.createSide(x, y + 1,
                            LockInfo.from(metadata));
                    activators.add(activator);
                    mark(x, y);
                }
            }
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
        return !base.isGround(x, y + 1) && !base.isGround(x, y - 1) // walls above and below
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
