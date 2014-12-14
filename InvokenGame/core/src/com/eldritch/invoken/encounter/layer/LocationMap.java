package com.eldritch.invoken.encounter.layer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.eldritch.invoken.encounter.NaturalVector2;

public class LocationMap extends TiledMap {
    private final TiledMapTile ground;
    private Set<NaturalVector2> activeTiles = null;
    
    public LocationMap(TiledMapTile ground) {
        this.ground = ground;
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
            LocationLayer existing = presentLayers.get(mapLayer.getName());
            if (existing != null) {
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
}
