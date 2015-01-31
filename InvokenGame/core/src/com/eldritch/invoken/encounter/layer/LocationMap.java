package com.eldritch.invoken.encounter.layer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.eldritch.invoken.activators.Activator;
import com.eldritch.invoken.encounter.ConnectedRoom;
import com.eldritch.invoken.encounter.NaturalVector2;
import com.eldritch.invoken.util.Settings;

public class LocationMap extends TiledMap {
    private final TiledMapTile ground;
    private Set<NaturalVector2> activeTiles = null;
    private final List<Activator> activators = new ArrayList<Activator>();
    private final TiledMap overlayMap = new TiledMap();
    private final Map<Texture, Texture> normals = new HashMap<Texture, Texture>();
    
    private ConnectedRoom[][] rooms;
    
    public LocationMap(TiledMapTile ground) {
        this.ground = ground;
    }
    
    public void setRooms(ConnectedRoom[][] rooms) {
        this.rooms = rooms;
    }
    
    public ConnectedRoom[][] getRooms() {
        return rooms;
    }
    
    public void addOverlay(LocationLayer layer) {
    	overlayMap.getLayers().add(layer);
    }
    
    public TiledMap getOverlayMap() {
    	return overlayMap;
    }
    
    public void add(Activator activator) {
    	activators.add(activator);
    }
    
    public List<Activator> getActivators() {
    	return activators;
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
    
    public TiledMapTile getGround() {
        return ground;
    }
    
    public int getWidth() {
        return getLayers().getCount() > 0 ? ((LocationLayer) (getLayers().get(0))).getWidth() : 0;
    }
    
    public int getHeight() {
        return getLayers().getCount() > 0 ? ((LocationLayer) (getLayers().get(0))).getHeight() : 0;
    }
    
    public Map<String, LocationLayer> getLayerMap() {
        Map<String, LocationLayer> map = new HashMap<String, LocationLayer>();
        for (MapLayer layer : getLayers()) {
            map.put(layer.getName(), (LocationLayer) layer);
        }
        return map;
    }
    
    public void merge(TiledMap map, NaturalVector2 offset) {
        Map<String, LocationLayer> presentLayers = getLayerMap();
        for (MapLayer mapLayer : map.getLayers()) {
            TiledMapTileLayer layer = (TiledMapTileLayer) mapLayer;
            if (layer.getName().equals("constraints")) {
                // don't add the constraints
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
