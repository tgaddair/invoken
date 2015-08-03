package com.eldritch.invoken.location.proc;

import java.util.HashSet;
import java.util.Set;

import com.eldritch.invoken.proto.Actors.Container;
import com.eldritch.invoken.proto.Actors.InventoryItem;
import com.eldritch.invoken.state.Inventory;
import com.eldritch.invoken.util.Constants;

public class LootGenerator {
    private final Set<InventoryItem> items = new HashSet<>();
    
    public LootGenerator() {
        Container container = Inventory.getContainer(Constants.RANDOM_LOOT);
        items.addAll(container.getItemList());
    }
    
    public Inventory generate() {
        Inventory inv = new Inventory();
        return inv;
    }
}
