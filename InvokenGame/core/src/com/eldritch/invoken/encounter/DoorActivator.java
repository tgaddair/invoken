package com.eldritch.invoken.encounter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.encounter.layer.LocationLayer;

public class DoorActivator implements Activator {
    private final Cell cell;
    private final Vector2 position = new Vector2();
    private final List<RemovableCell> cells = new ArrayList<RemovableCell>();
    private final TiledMapTile unlockedTile;
    private final TiledMapTile lockedTile;
    private boolean open = false;
    private boolean locked;

    public DoorActivator(int x, int y, Collection<RemovableCell> cells,
            TiledMapTile unlockedTile, TiledMapTile lockedTile, LocationLayer layer) {
        this.cell = new LocationCell(NaturalVector2.of(x, y), layer);
        position.set(x, y);
        this.cells.addAll(cells);
        this.unlockedTile = unlockedTile;
        this.lockedTile = lockedTile;
        
        locked = Math.random() < 0.5;
        resetCell();
    }

    @Override
    public boolean click(float x, float y) {
        boolean clicked = x >= position.x && x <= position.x + 1 && y >= position.y
                && y <= position.y + 1;
        if (clicked) {
            activate();
        }
        return clicked;
    }

    @Override
    public void activate() {
        if (locked) {
            // unlock
            locked = false;
            resetCell();
            return;
        }
        
        open = !open;
        for (RemovableCell cell : cells) {
            cell.set(open);
        }
    }
    
    public Cell getCell() {
        return cell;
    }
    
    private void resetCell() {
        cell.setTile(locked ? lockedTile : unlockedTile);
    }
}
