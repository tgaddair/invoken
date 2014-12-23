package com.eldritch.invoken.encounter.proc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.math.Rectangle;
import com.eldritch.invoken.encounter.NaturalVector2;
import com.eldritch.invoken.encounter.layer.LocationLayer;
import com.eldritch.invoken.encounter.layer.LocationMap;
import com.eldritch.invoken.encounter.proc.FurnitureLoader.PlaceableFurniture;

public class TmxPlaceableFurniture implements PlaceableFurniture {
	private final TiledMap tiles;
	
	public TmxPlaceableFurniture(TiledMap tiles) {
		this.tiles = tiles;
	}
	
	@Override
	public int getCost() {
		int cost = 0;
        TiledMapTileLayer layer = (TiledMapTileLayer) tiles.getLayers().get("collision");
        if (layer != null) {
            for (int x = 0; x < layer.getWidth(); x++) {
                for (int y = 0; y < layer.getHeight(); y++) {
                    Cell cell = layer.getCell(x, y);
                    if (cell != null) {
                        cost++;
                    }
                }
            }
        }
        return cost;
	}

	@Override
	public NaturalVector2 findPosition(Rectangle rect, LocationMap map) {
		Map<String, LocationLayer> presentLayers = map.getLayerMap();
        for (int x = (int) rect.x; x < rect.x + rect.width; x++) {
            for (int y = (int) rect.y; y < rect.y + rect.height; y++) {
                if (isContiguous(rect, tiles, x, y, map) 
                        && compatible(presentLayers, tiles, x, y)) {
                    return NaturalVector2.of(x, y);
                }
            }
        }
        return null;
	}

	@Override
	public void place(NaturalVector2 position, LocationMap map) {
		map.merge(tiles, position);
		
	}
	
	private boolean compatible(Map<String, LocationLayer> presentLayers, TiledMap furniture,
            int x, int y) {
        for (MapLayer mapLayer : furniture.getLayers()) {
            TiledMapTileLayer layer = (TiledMapTileLayer) mapLayer;
            if (layer.getName().equals("constraints")) {
                LocationLayer existing = presentLayers.get("base");
                if (existing == null) {
                    // no base layer to be in conflict with
                    continue;
                }
                
                // check that constraints are satisfied in the base layer
                for (int i = 0; i < layer.getWidth(); i++) {
                    for (int j = 0; j < layer.getHeight(); j++) {
                        Cell cell = layer.getCell(i, j);
                        if (cell != null) {
                            String constraint = cell.getTile().getProperties().get(
                                    "constraint", String.class);
                            if (constraint != null) {
                                if (constraint.equals("ground") 
                                        && !existing.isGround(x + i, y + j)) {
                                    // tile in base is required to be ground, but isn't
                                    return false;
                                }
                                if (constraint.equals("wall") 
                                        && !existing.isWall(x + i, y + j)) {
                                    // tile in base is required to be wall, but isn't
                                    return false;
                                }
                            }
                        }
                    }
                }
            } else {
                LocationLayer existing = presentLayers.get(mapLayer.getName());
                if (existing == null) {
                    // no equivalent layer to be in conflict with
                    continue;
                }
                
                for (int i = 0; i < layer.getWidth(); i++) {
                    for (int j = 0; j < layer.getHeight(); j++) {
                        if (!inBounds(x + i, y + j, existing)) {
                            // furniture will not fit, so call this a failure
                            return false;
                        }
                        
                        Cell current = existing.getCell(x + i, y + j);
                        Cell cell = layer.getCell(i, j);
                        if (current != null && cell != null) {
                            // conflict
                            return false;
                        }
                    }
                }
            }
        }
        
        return true;
    }
    
    private boolean isContiguous(Rectangle rect, TiledMap candidate, int x, int y, LocationMap map) {
        LocationLayer collision = (LocationLayer) map.getLayers().get("collision");
        TiledMapTileLayer collision2 = (TiledMapTileLayer) candidate.getLayers().get("collision");
        
        // check each entry point
        Set<NaturalVector2> visited = new HashSet<NaturalVector2>();
        LinkedList<NaturalVector2> points = getEntryPoints(rect, map);
        while (!points.isEmpty()) {
            NaturalVector2 point = points.removeFirst();
            if (visited.contains(point)) {
                // don't repeat work
                continue;
            }
            
            if (collision.getCell(point.x, point.y) == null) {
                // cell is free
                visited.add(point);
                
                if (!inBounds(x - point.x, y - point.y, collision2) 
                            || collision2.getCell(x - point.x, y - point.y) == null) {
                    for (NaturalVector2 neighbor : getNeighbors(point)) {
                        // add the neighbor if it's in the rectangle
                        if (neighbor.x >= rect.x && neighbor.x < rect.x + rect.width 
                                && neighbor.y >= rect.y && neighbor.y < rect.y + rect.height) {
                            points.add(neighbor);
                        }
                    }
                }
            }
        }
        
        return visited.containsAll(LocationGenerator.getFreeSpaces(collision, rect));
    }
    
    private List<NaturalVector2> getNeighbors(NaturalVector2 point) {
        List<NaturalVector2> neighbors = new ArrayList<NaturalVector2>();
        neighbors.add(NaturalVector2.of(point.x - 1, point.y));
        neighbors.add(NaturalVector2.of(point.x + 1, point.y));
        neighbors.add(NaturalVector2.of(point.x, point.y - 1));
        neighbors.add(NaturalVector2.of(point.x, point.y + 1));
        return neighbors;
    }
    
    private LinkedList<NaturalVector2> getEntryPoints(Rectangle rect, LocationMap map) {
        LinkedList<NaturalVector2> points = new LinkedList<NaturalVector2>();
        LocationLayer base = (LocationLayer) map.getLayers().get("base");
        for (int y = (int) rect.y; y < rect.y + rect.height; y++) {
            int x1 = (int) rect.x;
            if (base.isGround(x1 - 1, y)) {
                points.add(NaturalVector2.of(x1, y));
            }
            
            int x2 = (int) (rect.x + rect.width) - 1;
            if (base.isGround(x2 + 1, y)) {
                points.add(NaturalVector2.of(x2, y));
            }
        }
        for (int x = (int) rect.x; x < rect.x + rect.width; x++) {
            int y1 = (int) rect.y;
            if (base.isGround(x, y1 - 1)) {
                points.add(NaturalVector2.of(x, y1));
            }
            
            int y2 = (int) (rect.y + rect.height) - 1;
            if (base.isGround(x, y2 + 1)) {
                points.add(NaturalVector2.of(x, y2));
            }
        }
        return points;
    }
    
    private boolean inBounds(int x, int y, TiledMapTileLayer layer) {
        return x >= 0 && y >= 0 && x < layer.getWidth() && y < layer.getHeight();
    }
}
