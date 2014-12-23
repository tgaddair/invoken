package com.eldritch.invoken.activators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.encounter.NaturalVector2;
import com.eldritch.invoken.encounter.layer.LocationCell;
import com.eldritch.invoken.encounter.layer.LocationLayer;
import com.eldritch.invoken.encounter.layer.RemovableCell;

public class DoorActivator extends ClickActivator {
    private final Cell cell;
    private final List<RemovableCell> cells = new ArrayList<RemovableCell>();
    private final TiledMapTile unlockedTile;
    private final TiledMapTile lockedTile;
    private boolean open = false;
    private boolean locked;

    public DoorActivator(int x, int y, Collection<RemovableCell> cells,
            TiledMapTile unlockedTile, TiledMapTile lockedTile, LocationLayer layer) {
    	super(NaturalVector2.of(x, y));
        this.cell = new LocationCell(NaturalVector2.of(x, y), layer);
        this.cells.addAll(cells);
        this.unlockedTile = unlockedTile;
        this.lockedTile = lockedTile;
        
        locked = Math.random() < 0.5;
        resetCell();
    }

    @Override
    public void activate(Agent agent, Location location) {
        if (locked) {
            // unlock
            locked = false;
            resetCell();
            location.alertTo(agent);
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

	@Override
	public void register(Location location) {
	}
}
