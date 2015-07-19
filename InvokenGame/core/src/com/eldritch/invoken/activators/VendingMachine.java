package com.eldritch.invoken.activators;

import java.util.ArrayList;

import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.util.Lootable;
import com.eldritch.invoken.location.NaturalVector2;
import com.eldritch.invoken.proto.Actors.InventoryItem;
import com.eldritch.invoken.state.Inventory;

public class VendingMachine extends InteractableActivator implements Lootable {
    private final Inventory inventory;

    public VendingMachine(NaturalVector2 position) {
        this(position, new Inventory(new ArrayList<InventoryItem>()));
    }

    public VendingMachine(NaturalVector2 position, Inventory inventory) {
        super(position, 1, 2);
        this.inventory = inventory;
    }

    @Override
    protected void onBeginInteraction(Agent interactor) {
        interactor.barter(this);
    }

    @Override
    protected void onEndInteraction(Agent interactor) {
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
