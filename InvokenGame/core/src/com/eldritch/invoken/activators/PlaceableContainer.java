package com.eldritch.invoken.activators;

import java.lang.reflect.Constructor;

import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.location.NaturalVector2;
import com.eldritch.invoken.location.layer.LocationMap;
import com.eldritch.invoken.location.proc.FurnitureLoader.PlaceableFurniture;
import com.eldritch.invoken.location.proc.LootGenerator;
import com.eldritch.invoken.proto.Locations.Furniture;
import com.eldritch.invoken.state.Inventory;
import com.eldritch.invoken.util.Constants;
import com.google.common.base.Strings;

public class PlaceableContainer extends PlaceableActivator {
    private final LootGenerator lootGenerator;
    private final String asset;

    public PlaceableContainer(Furniture data, PlaceableFurniture tiles, LootGenerator lootGenerator) {
        super(data, tiles);
        this.lootGenerator = lootGenerator;
        this.asset = data.getAssetId();
    }

    @Override
    protected Activator load(String name, NaturalVector2 position, LocationMap map) {
        Inventory inventory;
        if (Strings.isNullOrEmpty(asset) || asset.equals(Constants.RANDOM_LOOT)) {
            inventory = lootGenerator.generate(position);
        } else {
            inventory = Inventory.from(asset);
        }
        
        try {
            Class<?> clazz = getClassByName(name);
            Constructor<?> ctor = clazz.getConstructor(NaturalVector2.class, Inventory.class);
            Activator instance = (Activator) ctor.newInstance(position, inventory);
            return instance;
        } catch (Exception ex) {
            InvokenGame.error("Could not load container: " + name, ex);
            return null;
        }
    }
}
