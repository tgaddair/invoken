package com.eldritch.invoken.encounter.proc;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.activators.Activator;
import com.eldritch.invoken.activators.DoorActivator;
import com.eldritch.invoken.encounter.NaturalVector2;
import com.eldritch.invoken.encounter.layer.LocationLayer;
import com.eldritch.invoken.encounter.layer.LocationLayer.CollisionLayer;
import com.eldritch.invoken.encounter.layer.LocationCell;
import com.eldritch.invoken.encounter.layer.LocationMap;
import com.eldritch.invoken.encounter.layer.RemovableCell;
import com.eldritch.invoken.gfx.Light;
import com.eldritch.invoken.gfx.Light.StaticLight;
import com.eldritch.invoken.util.Settings;

public abstract class FurnitureGenerator {
    private final Set<NaturalVector2> marked = new HashSet<NaturalVector2>();
    private final TextureAtlas atlas;
    protected final TiledMapTile ground;
    
    public FurnitureGenerator(TextureAtlas atlas, TiledMapTile ground) {
        this.atlas = atlas;
        this.ground = ground;
    }
    
    public void addLights(LocationLayer layer, LocationLayer base, List<Light> lights, TiledMapTile placer) {
        TiledMapTile light = new StaticTiledMapTile(atlas.findRegion("test-biome/light1"));
        light.setOffsetY(-Settings.PX / 2);
        for (int y = 0; y < base.getHeight(); y++) {
            // scan by row so we can properly distribute lights
            int lastLight = 0;
            for (int x = 0; x < base.getWidth(); x++) {
                Cell cell = base.getCell(x, y);
                if (cell != null && cell.getTile() == placer && !isMarked(x, y)) {
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
    
    public void createDoors(LocationLayer base, LocationLayer trim,
            LocationLayer overlay, LocationLayer overlayTrim, CollisionLayer collision,
            List<Activator> activators) {
        addDoors(base, trim, overlay, collision, activators);
        addTrimDoors(base, trim, overlayTrim, collision, activators);
    }

    private void addDoors(LocationLayer base, LocationLayer trim,
            LocationLayer overlay, CollisionLayer collision, List<Activator> activators) {
        // add front doors
        for (int x = 0; x < base.getWidth(); x++) {
            for (int y = 0; y < base.getHeight(); y++) {
                if (base.isGround(x, y) && base.isGround(x + 1, y)) {
                    // wall to the left, wall to the right
                    if (isLowerGap(x, y, base)) {
                        // add activator
                        DoorActivator activator = DoorActivator.createFront(x, y);
                        activators.add(activator);
                        mark(x, y);
                    }
                }
            }
        }
    }
    
    private boolean isLowerGap(int x, int y, LocationLayer base) {
        return 
                !base.isGround(x - 1, y) && !base.isGround(x + 2, y) &&
                !base.isGround(x - 1, y + 1) && !base.isGround(x + 2, y + 1) &&
                (base.isGround(x - 1, y - 1) || base.isGround(x + 2, y - 1));
    }

    private void addTrimDoors(LocationLayer base, LocationLayer trim,
            LocationLayer overlayTrim, CollisionLayer collision, List<Activator> activators) {
        // add side doors
        for (int x = 0; x < base.getWidth(); x++) {
            for (int y = 0; y < base.getHeight(); y++) {
                if (base.isGround(x, y)) {
                    // wall up, wall down
                    if (isSideGap(x, y, base)) {
                        // add activator
                        InvokenGame.log("side door");
                        DoorActivator activator = DoorActivator.createSide(x, y + 1);
                        activators.add(activator);
                        mark(x, y);
                    }
                }
            }
        }
    }
    
    private boolean isSideGap(int x, int y, LocationLayer base) {
        return 
                !base.isGround(x, y + 1) && !base.isGround(x, y - 1) &&
                (base.isGround(x - 1, y + 1) || base.isGround(x + 1, y + 1));
    }
    
    private void mark(int x, int y) {
        marked.add(NaturalVector2.of(x, y));
    }
    
    public boolean isMarked(int x, int y) {
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
