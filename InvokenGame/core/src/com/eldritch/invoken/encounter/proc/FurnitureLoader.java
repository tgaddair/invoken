package com.eldritch.invoken.encounter.proc;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Rectangle;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.activators.PlaceableActivator;
import com.eldritch.invoken.encounter.NaturalVector2;
import com.eldritch.invoken.encounter.layer.LocationMap;
import com.eldritch.invoken.proto.Locations.Room.Furniture;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public abstract class FurnitureLoader {
    private static final Map<Furniture.Type, FurnitureLoader> loaders = 
            new HashMap<Furniture.Type, FurnitureLoader>();
    
    static {
        loaders.put(Furniture.Type.TMX, new TmxFurnitureLoader());
        loaders.put(Furniture.Type.ACTIVATOR, new ActivatorLoader());
    }
    
    public abstract PlaceableFurniture load(String assetId);
    
    public interface PlaceableFurniture {
    	int getCost();
    	
    	NaturalVector2 findPosition(Rectangle rect, LocationMap map);
    	
    	void place(NaturalVector2 position, LocationMap map);
    }
    
    public static class TmxFurnitureLoader extends FurnitureLoader {
        private final LoadingCache<String, TmxPlaceableFurniture> cache = CacheBuilder.newBuilder()
                .build(new CacheLoader<String, TmxPlaceableFurniture>() {
                  public TmxPlaceableFurniture load(String assetId) {
                	  TiledMap tiles = new TmxMapLoader().load("furniture/" + assetId + ".tmx");
                      return new TmxPlaceableFurniture(tiles);
                  }
                });
        
        @Override
        public PlaceableFurniture load(String assetId) {
            try {
                return cache.get(assetId);
            } catch (ExecutionException ex) {
                InvokenGame.error("Failed to load furniture from TMX file: " + assetId, ex);
                return null;
            }
        }
    }
    
    public static class ActivatorLoader extends FurnitureLoader {
    	@Override
    	public PlaceableFurniture load(String assetId) {
    		FurnitureLoader tmxLoader = FurnitureLoader.getLoader(Furniture.Type.TMX);
    		PlaceableFurniture furniture = tmxLoader.load("activators/" + assetId);
    		return new PlaceableActivator(assetId, furniture);
    	}
    }
    
    public static PlaceableFurniture load(Furniture furniture) {
        return getLoader(furniture.getType()).load(furniture.getId());
    }
    
    public static FurnitureLoader getLoader(Furniture.Type type) {
        return loaders.get(type);
    }
    
    protected FurnitureLoader() {}
}
