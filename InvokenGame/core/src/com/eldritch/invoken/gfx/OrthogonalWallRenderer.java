package com.eldritch.invoken.gfx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.eldritch.invoken.location.layer.LocationLayer;
import com.eldritch.invoken.location.layer.LocationLayer.WallLayer;

public class OrthogonalWallRenderer extends OrthogonalShadedTiledMapRenderer {
    private final Map<Integer, List<LocationLayer>> zLayers = new HashMap<>();
    
    public OrthogonalWallRenderer(TiledMap map, float unitScale, NormalMapShader shader) {
        super(map, unitScale, shader);
        
        for (MapLayer mapLayer : map.getLayers()) {
            if (mapLayer instanceof WallLayer) {
                WallLayer layer = (WallLayer) mapLayer;
                if (!zLayers.containsKey(layer.getZ())) {
                    zLayers.put(layer.getZ(), new ArrayList<LocationLayer>());
                }
                zLayers.get(layer.getZ()).add(layer);
            }
        }
    }
    
    public void render(int startZ, int endZ) {
        beginRender();
        for (int z = startZ; z <= endZ; z++) {
            if (zLayers.containsKey(z)) {
                for (LocationLayer layer : zLayers.get(z)) {
                    renderTileLayer(layer);
                }
            }
        }
        endRender();
    }
}
