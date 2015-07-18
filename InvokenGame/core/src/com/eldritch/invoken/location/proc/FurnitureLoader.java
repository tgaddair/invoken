package com.eldritch.invoken.location.proc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.activators.PlaceableActivator;
import com.eldritch.invoken.activators.PlaceableContainer;
import com.eldritch.invoken.activators.PlaceableTerminal;
import com.eldritch.invoken.actor.type.InanimateEntity;
import com.eldritch.invoken.location.ConnectedRoom;
import com.eldritch.invoken.location.NaturalVector2;
import com.eldritch.invoken.location.layer.LocationMap;
import com.eldritch.invoken.proto.Locations.Furniture;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public abstract class FurnitureLoader {
    private static final Map<Furniture.Type, FurnitureLoader> loaders = 
            new HashMap<Furniture.Type, FurnitureLoader>();
    
    static {
        loaders.put(Furniture.Type.TMX, new TmxFurnitureLoader());
        loaders.put(Furniture.Type.ACTIVATOR, new ActivatorLoader());
        loaders.put(Furniture.Type.CONTAINER, new ContainerLoader());
        loaders.put(Furniture.Type.TERMINAL, new TerminalLoader());
    }
    
    public abstract PlaceableFurniture loadFrom(Furniture furniture);
    
    public interface PlaceableFurniture {
    	int getCost();
    	
    	NaturalVector2 findPosition(ConnectedRoom room, LocationMap map, Random rand);
    	
    	List<InanimateEntity> place(NaturalVector2 position, LocationMap map);
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
        public PlaceableFurniture loadFrom(Furniture furniture) {
        	String id = furniture.getId();
            try {
                return cache.get(id);
            } catch (ExecutionException ex) {
                InvokenGame.error("Failed to load furniture from TMX file: " + id, ex);
                return null;
            }
        }
    }
    
    public static class ActivatorLoader extends FurnitureLoader {
    	@Override
    	public PlaceableFurniture loadFrom(Furniture furniture) {
    		FurnitureLoader tmxLoader = FurnitureLoader.getLoader(Furniture.Type.TMX);
    		PlaceableFurniture placeable = tmxLoader.loadFrom(furniture);
    		return new PlaceableActivator(furniture, placeable);
    	}
    }
    
    public static class ContainerLoader extends FurnitureLoader {
        @Override
        public PlaceableFurniture loadFrom(Furniture furniture) {
            FurnitureLoader tmxLoader = FurnitureLoader.getLoader(Furniture.Type.TMX);
            PlaceableFurniture placeable = tmxLoader.loadFrom(furniture);
            return new PlaceableContainer(furniture, placeable);
        }
    }
    
    public static class TerminalLoader extends FurnitureLoader {
        @Override
        public PlaceableFurniture loadFrom(Furniture furniture) {
            FurnitureLoader tmxLoader = FurnitureLoader.getLoader(Furniture.Type.TMX);
            PlaceableFurniture placeable = tmxLoader.loadFrom(furniture);
            return new PlaceableTerminal(furniture, placeable);
        }
    }
    
    public static PlaceableFurniture load(Furniture furniture) {
        return getLoader(furniture.getType()).loadFrom(furniture);
    }
    
    public static FurnitureLoader getLoader(Furniture.Type type) {
        if (!loaders.containsKey(type)) {
            throw new IllegalArgumentException("No loader for type: " + type);
        }
        return loaders.get(type);
    }
    
    protected FurnitureLoader() {}
}
