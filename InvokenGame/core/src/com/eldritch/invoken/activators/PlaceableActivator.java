package com.eldritch.invoken.activators;

import java.lang.reflect.Constructor;
import java.util.Random;

import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.encounter.ConnectedRoom;
import com.eldritch.invoken.encounter.NaturalVector2;
import com.eldritch.invoken.encounter.layer.LocationMap;
import com.eldritch.invoken.encounter.proc.FurnitureLoader.PlaceableFurniture;
import com.eldritch.invoken.proto.Locations.Room.Furniture;
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
	public NaturalVector2 findPosition(ConnectedRoom room, LocationMap map, Random rand) {
		return tiles.findPosition(room, map, rand);
	}

	@Override
	public void place(NaturalVector2 position, LocationMap map) {
		tiles.place(position, map);
		map.add(load(data.getId(), position, map));
	}
	
	public static Activator load(String name, NaturalVector2 position, LocationMap map) {
		try {
			String assetId = CaseFormat.LOWER_HYPHEN.to(CaseFormat.UPPER_CAMEL, name);
			String path = PlaceableActivator.class.getPackage().getName();
//			InvokenGame.log("loading: " + path + "." + assetId);
			Class<?> clazz = Class.forName(path + "." + assetId);
			Constructor<?> ctor = clazz.getConstructor(NaturalVector2.class);
			Activator instance = (Activator) ctor.newInstance(position);
			return instance;
		} catch (Exception ex) {
			InvokenGame.error("Could not load activator: " + name, ex);
			return null;
		}
	}
}
