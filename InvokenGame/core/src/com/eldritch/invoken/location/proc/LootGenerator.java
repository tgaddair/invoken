package com.eldritch.invoken.location.proc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.items.Item;
import com.eldritch.invoken.proto.Actors.Container;
import com.eldritch.invoken.proto.Actors.InventoryItem;
import com.eldritch.invoken.state.Inventory;
import com.eldritch.invoken.util.Constants;

public class LootGenerator {
    private static final int MINOR_VALUE = 100;
    
    private final List<InventoryItem> items = new ArrayList<>();
    private final Random rand = new Random();  // don't seed the RNG for different loot between loads
    
    private final int totalTargetValue;
    private int totalValue = 0;
    
    public LootGenerator(int floor) {
        Container container = Inventory.getContainer(Constants.RANDOM_LOOT);
        items.addAll(container.getItemList());
        
        this.totalTargetValue = (int) (floor * 25 + Math.log(floor + 3) * 150 + 250);
    }
    
    public Inventory generate() {
        int targetValue = (int) (rand.nextDouble() * (totalTargetValue - totalValue));
        targetValue = Math.max(targetValue - totalValue, MINOR_VALUE);
        int currentValue = 0;
        
        Inventory inv = new Inventory();
        Collections.shuffle(items);
        for (InventoryItem proto : items) {
            int count = Inventory.getDesiredCount(proto, rand);
            if (count > 0) {
                Item item = Item.fromProto(InvokenGame.ITEM_READER.readAsset(proto.getItemId()));
                
                int addedValue = item.getValue() * count;
                if (currentValue + addedValue < targetValue) {
                    // go ahead and add all the items
                    inv.addItem(item, count);
                    currentValue += addedValue;
                    
                    // check our current value
                    if (currentValue >= targetValue) {
                        break;
                    }
                }
            }
        }
        
        totalValue += currentValue;
        return inv;
    }
}
