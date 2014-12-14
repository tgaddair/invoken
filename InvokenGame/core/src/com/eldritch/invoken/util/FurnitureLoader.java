package com.eldritch.invoken.util;

import java.util.HashMap;
import java.util.Map;

import com.eldritch.invoken.proto.Locations.Room.Furniture;

public abstract class FurnitureLoader {
    private static final Map<Furniture.Type, FurnitureLoader> loaders = 
            new HashMap<Furniture.Type, FurnitureLoader>();
    
    static {
        loaders.put(Furniture.Type.TMX, new TmxFurnitureLoader());
    }
    
    public abstract void load(String assetId);
    
    public static class TmxFurnitureLoader extends FurnitureLoader {
        @Override
        public void load(String assetId) {
        }
    }
    
    public static FurnitureLoader getLoader(Furniture.Type type) {
        return loaders.get(type);
    }
    
    private FurnitureLoader() {}
}
