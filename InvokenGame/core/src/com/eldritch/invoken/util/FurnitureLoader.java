package com.eldritch.invoken.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.proto.Locations.Room.Furniture;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public abstract class FurnitureLoader {
    private static final Map<Furniture.Type, FurnitureLoader> loaders = 
            new HashMap<Furniture.Type, FurnitureLoader>();
    
    static {
        loaders.put(Furniture.Type.TMX, new TmxFurnitureLoader());
    }
    
    public abstract TiledMap load(String assetId);
    
    public static class TmxFurnitureLoader extends FurnitureLoader {
        private final LoadingCache<String, TiledMap> cache = CacheBuilder.newBuilder()
                .build(new CacheLoader<String, TiledMap>() {
                  public TiledMap load(String assetId) {
                      return new TmxMapLoader().load("furniture/" + assetId + ".tmx");
                  }
                });
        
        @Override
        public TiledMap load(String assetId) {
            try {
                return cache.get(assetId);
            } catch (ExecutionException ex) {
                InvokenGame.error("Failed to load furniture from TMX file: " + assetId, ex);
                return null;
            }
        }
    }
    
    public static TiledMap load(Furniture furniture) {
        return getLoader(furniture.getType()).load(furniture.getId());
    }
    
    public static FurnitureLoader getLoader(Furniture.Type type) {
        return loaders.get(type);
    }
    
    private FurnitureLoader() {}
}
