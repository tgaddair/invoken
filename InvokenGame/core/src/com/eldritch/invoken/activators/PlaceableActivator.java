package com.eldritch.invoken.activators;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.maps.MapProperties;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.type.InanimateEntity;
import com.eldritch.invoken.location.ConnectedRoom;
import com.eldritch.invoken.location.NaturalVector2;
import com.eldritch.invoken.location.layer.LocationMap;
import com.eldritch.invoken.location.proc.FurnitureLoader.PlaceableFurniture;
import com.eldritch.invoken.proto.Locations.Furniture;
import com.eldritch.invoken.util.Constants;
import com.google.common.base.CaseFormat;

public class PlaceableActivator implements PlaceableFurniture {
	private final Furniture data;
	private final PlaceableFurniture tiles;
	
	public PlaceableActivator(Furniture data, PlaceableFurniture tiles) {
		this.data = data;
		this.tiles = tiles;
	}
	
	@Override
	public int getCost() {
		return tiles.getCost();
	}

    @Override
    public MapProperties getProperties() {
        return tiles.getProperties();
    }

	@Override
	public NaturalVector2 findPosition(ConnectedRoom room, LocationMap map, Random rand) {
		return tiles.findPosition(room, map, rand);
	}

	@Override
	public List<InanimateEntity> place(NaturalVector2 position, LocationMap map) {
	    String id = data.getId();
	    if (tiles.getProperties().containsKey(Constants.ACTIVATOR)) {
	        // override default ID
	        id = tiles.getProperties().get(Constants.ACTIVATOR, String.class);
	    }
	    
		List<InanimateEntity> entities = tiles.place(position, map);
		Activator activator = load(id, position, map);
		activator.register(entities);
		map.add(activator);
		return entities;
	}
	
	protected Activator load(String name, NaturalVector2 position, LocationMap map) {
		try {
			Class<?> clazz = getClassByName(name);
			Constructor<?> ctor = clazz.getConstructor(NaturalVector2.class);
			Activator instance = (Activator) ctor.newInstance(position);
			return instance;
		} catch (Exception ex) {
			InvokenGame.error("Could not load activator: " + name, ex);
			return null;
		}
	}
	
	protected static Class<?> getClassByName(String name) {
	    try {
    	    String assetId = CaseFormat.LOWER_HYPHEN.to(CaseFormat.UPPER_CAMEL, name);
            String path = PlaceableActivator.class.getPackage().getName();
    //      InvokenGame.log("loading: " + path + "." + assetId);
            Class<?> clazz = Class.forName(path + "." + assetId);
            return clazz;
	    } catch (Exception ex) {
            InvokenGame.error("Could not retrieve concrete class for activator: " + name, ex);
            return null;
        }
	}
}
