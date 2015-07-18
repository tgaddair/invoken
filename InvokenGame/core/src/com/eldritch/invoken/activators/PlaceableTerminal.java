package com.eldritch.invoken.activators;

import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.location.NaturalVector2;
import com.eldritch.invoken.location.layer.LocationMap;
import com.eldritch.invoken.location.proc.FurnitureLoader.PlaceableFurniture;
import com.eldritch.invoken.proto.Actors.Terminal;
import com.eldritch.invoken.proto.Locations.Furniture;

public class PlaceableTerminal extends PlaceableActivator {
    private final Terminal terminal;

    public PlaceableTerminal(Furniture data, PlaceableFurniture tiles) {
        super(data, tiles);
        this.terminal = InvokenGame.TERMINAL_READER.readAsset(data.getAssetId());
    }

    @Override
    protected Activator load(String name, NaturalVector2 position, LocationMap map) {
        return new InfoTerminal(position, terminal);
    }
}
