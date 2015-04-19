package com.eldritch.invoken.activators;

import java.lang.reflect.Constructor;

import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.location.NaturalVector2;
import com.eldritch.invoken.location.layer.LocationMap;
import com.eldritch.invoken.location.proc.FurnitureLoader.PlaceableFurniture;
import com.eldritch.invoken.proto.Locations.Room.Furniture;
import com.eldritch.invoken.state.Inventory;
import com.google.common.base.CaseFormat;

public class PlaceableContainer extends PlaceableActivator {
	private final Inventory container;
	
	public PlaceableContainer(Furniture data, PlaceableFurniture tiles) {
	    super(data, tiles);
	    container = Inventory.from(data.getAssetId());
	}
	
	@Override
	protected Activator load(String name, NaturalVector2 position, LocationMap map) {
		try {
			String assetId = CaseFormat.LOWER_HYPHEN.to(CaseFormat.UPPER_CAMEL, name);
			String path = PlaceableContainer.class.getPackage().getName();
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
