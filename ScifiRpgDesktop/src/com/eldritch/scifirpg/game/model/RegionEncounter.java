package com.eldritch.scifirpg.game.model;

import java.util.List;

import com.eldritch.scifirpg.proto.Locations.Encounter;
import com.eldritch.scifirpg.proto.Locations.Encounter.RegionParams;
import com.eldritch.scifirpg.proto.Locations.Encounter.RegionParams.Cell;

public class RegionEncounter extends AbstractEncounter {
    private final RegionParams params;
    
	public RegionEncounter(Encounter data) {
		super(data);
		this.params = data.getRegionParams();
	}
	
	public int getRowCount() {
	    return params.getRowLength();
	}
	
	public int getColumnCount() {
	    Cell lastCell = params.getCell(params.getCellCount() - 1);
	    return (lastCell.getPosition() / getRowCount()) + 1;
	}
	
	public List<Cell> getCells() {
	    return params.getCellList();
	}
}
