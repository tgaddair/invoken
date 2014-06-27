package com.eldritch.invoken.encounter.proc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.eldritch.invoken.encounter.Activator;
import com.eldritch.invoken.encounter.DoorActivator;
import com.eldritch.invoken.encounter.NaturalVector2;
import com.eldritch.invoken.encounter.layer.LocationLayer;
import com.eldritch.invoken.encounter.layer.LocationLayer.CollisionLayer;
import com.eldritch.invoken.encounter.layer.LocationCell;
import com.eldritch.invoken.encounter.layer.LocationMap;
import com.eldritch.invoken.encounter.layer.RemovableCell;

public abstract class FurnitureGenerator {
    private final Set<NaturalVector2> marked = new HashSet<NaturalVector2>();
    private final TextureAtlas atlas;
    
    // door tiles
    private final TiledMapTile doorLeft;
    private final TiledMapTile doorRight;
    private final TiledMapTile doorOverLeft;
    private final TiledMapTile doorOverRight;
    private final TiledMapTile doorOverLeftTop;
    private final TiledMapTile doorOverRightTop;
    private final TiledMapTile unlockedDoor;
    private final TiledMapTile lockedDoor;
    
    public FurnitureGenerator(TextureAtlas atlas) {
        this.atlas = atlas;
        doorLeft = new StaticTiledMapTile(atlas.findRegion("test-biome/door-front-bottom-left"));
        doorRight = new StaticTiledMapTile(atlas.findRegion("test-biome/door-front-bottom-right"));
        doorOverLeft = new StaticTiledMapTile(atlas.findRegion("test-biome/door-over-left"));
        doorOverRight = new StaticTiledMapTile(atlas.findRegion("test-biome/door-over-right"));
        doorOverLeftTop = new StaticTiledMapTile(atlas.findRegion("test-biome/door-over-left-top"));
        doorOverRightTop = new StaticTiledMapTile(
                atlas.findRegion("test-biome/door-over-right-top"));
        unlockedDoor = new StaticTiledMapTile(atlas.findRegion("test-biome/door-activator"));
        lockedDoor = new StaticTiledMapTile(atlas.findRegion("test-biome/door-activator-locked"));
    }
    
    public void createDoors(LocationLayer base, LocationLayer trim,
            LocationLayer overlay, LocationLayer overlayTrim, CollisionLayer collision,
            List<Activator> activators) {
        addDoors(base, trim, overlay, collision, activators);
        addTrimDoors(base, trim, overlayTrim, collision, activators);
    }

    private void addDoors(LocationLayer base, LocationLayer trim,
            LocationLayer overlay, CollisionLayer collision, List<Activator> activators) {

        TiledMapTile doorTopLeft = new StaticTiledMapTile(
                atlas.findRegion("test-biome/door-front-top-left"));
        TiledMapTile doorTopRight = new StaticTiledMapTile(
                atlas.findRegion("test-biome/door-front-top-right"));

        // add front doors
        List<RemovableCell> cells = new ArrayList<RemovableCell>();
        for (int x = 0; x < base.getWidth(); x++) {
            for (int y = 0; y < base.getHeight(); y++) {
                Cell cell = base.getCell(x, y);
                if (base.isGround(cell)) {
                    // wall to the left, wall to the right
                    if (base.isWall(x - 4, y) && base.isWall(x + 1, y)) {
                        if (base.isGround(x - 4, y - 1) && base.isGround(x + 1, y - 1)) {
                            // room below
                            addCell(trim, doorLeft, x - 3, y, cells);
                            addCell(trim, doorRight, x - 2, y, cells);
                            addCell(trim, doorLeft, x - 1, y, cells);
                            addCell(trim, doorRight, x, y, cells);

                            // add overlay
                            addCell(overlay, doorTopLeft, x - 3, y + 1, cells);
                            addCell(overlay, doorTopRight, x - 2, y + 1, cells);
                            addCell(overlay, doorTopLeft, x - 1, y + 1, cells);
                            addCell(overlay, doorTopRight, x, y + 1, cells);

                            // add collision
                            collision.addCell(x - 3, y, cells, this);
                            collision.addCell(x - 2, y, cells, this);
                            collision.addCell(x - 1, y, cells, this);
                            collision.addCell(x, y, cells, this);

                            // add activator
                            DoorActivator activator = new DoorActivator(x - 4, y + 1, cells,
                                    unlockedDoor, lockedDoor, trim);
                            activators.add(activator);
                            trim.setCell(x - 4, y + 1, activator.getCell());
                            mark(x - 4, y + 1);
                            cells.clear();
                        }
                    }
                }
            }
        }
    }

    private void addTrimDoors(LocationLayer base, LocationLayer trim,
            LocationLayer overlayTrim, CollisionLayer collision, List<Activator> activators) {

        List<RemovableCell> cells = new ArrayList<RemovableCell>();
        for (int x = 0; x < base.getWidth(); x++) {
            for (int y = 0; y < base.getHeight(); y++) {
                Cell cell = base.getCell(x, y);
                if (base.isGround(cell)) {
                    // wall up, wall down
                    if (overlayTrim.hasCell(x, y - 2) && base.isWall(x, y + 1)) {
                        if (base.isGround(x - 1, y - 2) && base.isGround(x - 1, y + 1)) {
                            // room left
                            addTrimDoor(trim, overlayTrim, collision, doorOverLeft,
                                    doorOverLeftTop, x, y, activators, cells);
                        } else if (base.isGround(x + 1, y - 2) && base.isGround(x + 1, y + 1)
                                && trim.getCell(x, y + 2) == null) {
                            // room right and no pre-existing door panel in the way
                            addTrimDoor(trim, overlayTrim, collision, doorOverRight,
                                    doorOverRightTop, x, y, activators, cells);
                        }
                    }
                }
            }
        }
    }

    private void addTrimDoor(LocationLayer trim, LocationLayer overlayTrim,
            CollisionLayer collision, TiledMapTile tile, TiledMapTile top, int x, int y,
            List<Activator> activators, List<RemovableCell> cells) {
        // add the doors
        addCell(overlayTrim, tile, x, y - 1, cells);
        addCell(overlayTrim, tile, x, y, cells);
        addCell(overlayTrim, tile, x, y + 1, cells);
        addCell(overlayTrim, tile, x, y + 2, cells);
        addCell(overlayTrim, top, x, y + 3, cells);

        // add collision if absent so we don't delete collision cells when the door comes down
        collision.addCellIfAbsent(x, y - 2, cells, this);
        collision.addCellIfAbsent(x, y - 1, cells, this);
        collision.addCellIfAbsent(x, y, cells, this);
        collision.addCellIfAbsent(x, y + 1, cells, this);
        collision.addCellIfAbsent(x, y + 2, cells, this);
        collision.addCellIfAbsent(x, y + 3, cells, this);

        // add activator
        DoorActivator activator = new DoorActivator(x, y + 2, cells, unlockedDoor, lockedDoor, trim);
        activators.add(activator);
        trim.setCell(x, y + 2, activator.getCell());
        mark(x, y + 2);
        cells.clear();
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
    
    public abstract LocationLayer generateClutter(LocationLayer base, TiledMapTile ground, LocationMap map);
}
