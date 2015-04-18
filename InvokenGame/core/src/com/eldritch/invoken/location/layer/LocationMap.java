package com.eldritch.invoken.location.layer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.eldritch.invoken.activators.Activator;
import com.eldritch.invoken.actor.type.CoverPoint;
import com.eldritch.invoken.actor.type.DynamicEntity;
import com.eldritch.invoken.location.ConnectedRoomManager;
import com.eldritch.invoken.location.NaturalVector2;
import com.eldritch.invoken.location.layer.LocationLayer.CollisionLayer;
import com.eldritch.invoken.util.Settings;
import com.google.common.collect.Lists;

public class LocationMap extends TiledMap {
	private enum Type {
		Ground, Wall, Object, LowWall, ShortObject
	}
	
	// TODO: maintain a map of visited connected rooms and use it as a minimap for the player
	private final Type[][] typeMap;
	private final int[][] lightWalls;
	
    private final TiledMapTile ground;
    private final int width;
    private final int height;
    private Set<NaturalVector2> activeTiles = null;
    private final List<Activator> activators = new ArrayList<Activator>();
    private final List<DynamicEntity> entities = Lists.newArrayList();
    private final List<CoverPoint> coverPoints = new ArrayList<CoverPoint>();
    private final TiledMap overlayMap = new TiledMap();

    private ConnectedRoomManager rooms;
    
    // lazy creation
    private CollisionLayer collision = null;
    
    public LocationMap(TiledMapTile ground, int width, int height) {
        this.ground = ground;
        this.width = width;
        this.height = height;
        typeMap = new Type[width][height];
        lightWalls = new int[width][height];
    }
    
    public void setWall(int x, int y) {
    	typeMap[x][y] = Type.Wall;
    }
    
    public boolean isWall(int x, int y) {
    	return typeMap[x][y] == Type.Wall;
    }
    
    public void setRooms(ConnectedRoomManager rooms) {
        this.rooms = rooms;
    }
    
    public void addAllCover(List<CoverPoint> points) {
        coverPoints.addAll(points);
    }
    
    public List<CoverPoint> getCover() {
        return coverPoints;
    }
    
    public ConnectedRoomManager getRooms() {
        return rooms;
    }
    
    public boolean inBounds(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }
    
    public boolean isStrongLightWall(int x, int y) {
        return lightWalls[x][y] > 1;
    }
    
    public boolean isLightWall(int x, int y) {
        return lightWalls[x][y] > 0;
    }
    
    public void setLightWall(int x, int y, boolean value) {
        lightWalls[x][y] += value ? 1 : -1;
    }
    
    public void addOverlay(LocationLayer layer) {
    	overlayMap.getLayers().add(layer);
    	for (int i = 0; i < layer.getWidth(); i++) {
    	    for (int j = 0; j < layer.getHeight(); j++) {
    	        if (layer.isFilled(i, j)) {
    	            lightWalls[i][j] = 2;  // strong light wall
    	        }
    	    }
    	}
    }
    
    public TiledMap getOverlayMap() {
    	return overlayMap;
    }
    
    public void add(Activator activator) {
    	activators.add(activator);
    }
    
    public void addEntity(DynamicEntity entity) {
        entities.add(entity);
    }
    
    public List<Activator> getActivators() {
    	return activators;
    }
    
    public List<DynamicEntity> getEntities() {
        return entities;
    }
    
    public void update(Set<NaturalVector2> activeTiles) {
        this.activeTiles = activeTiles;
    }
    
    public boolean isActive(int x, int y) {
        if (activeTiles == null) {
            // during initialization
            return true;
        }
        return activeTiles.contains(NaturalVector2.of(x, y));
    }
    
    public boolean isClearGround(int x, int y) {
    	return isGround(x, y) && !getCollisionLayer().hasCell(x, y);
    }
    
    public boolean isGround(int x, int y) {
    	LocationLayer base = (LocationLayer) getLayers().get(0);
    	return base.isGround(x, y);
    }
    
    public LocationLayer getCollisionLayer() {
    	if (collision == null) {
    		collision = (CollisionLayer) getLayers().get("collision");
    	}
    	return collision;
    }
    
    public TiledMapTile getGround() {
        return ground;
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
    
    public Map<String, LocationLayer> getLayerMap() {
        Map<String, LocationLayer> map = new LinkedHashMap<String, LocationLayer>();
        for (MapLayer layer : getLayers()) {
            map.put(layer.getName(), (LocationLayer) layer);
        }
        for (MapLayer layer : overlayMap.getLayers()) {
            map.put(layer.getName(), (LocationLayer) layer);
        }
        return map;
    }
    
    public void merge(TiledMap map, NaturalVector2 offset) {
        Map<String, LocationLayer> presentLayers = getLayerMap();
        for (MapLayer mapLayer : map.getLayers()) {
            TiledMapTileLayer layer = (TiledMapTileLayer) mapLayer;
            if (layer.getName().startsWith("constraints")) {
                // don't add the constraints
                continue;
            }
            
            if (layer.getName().startsWith("dynamics")) {
                // add dynamic entities separately
                addEntity(new DynamicEntity(layer, offset));
                continue;
            }
            
            LocationLayer existing = presentLayers.get(mapLayer.getName());
            if (existing == null) {
                existing = new LocationLayer(getWidth(), getHeight(), 
                        Settings.PX, Settings.PX, this);
                existing.setVisible(true);
                existing.setOpacity(1.0f);
                existing.setName(layer.getName());
                
                // add the new layer
                getLayers().add(existing);
            }
            
            // merge the new layer into the existing
            for (int x = 0; x < layer.getWidth(); x++) {
                for (int y = 0; y < layer.getHeight(); y++) {
                    Cell cell = layer.getCell(x, y);
                    if (cell != null) {
                        // add this cell at the offset position
                        existing.addCell(cell.getTile(), offset.x + x, offset.y + y);
                    }
                }
            }
        }
    }
}