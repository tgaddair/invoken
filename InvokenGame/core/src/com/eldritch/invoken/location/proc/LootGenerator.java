package com.eldritch.invoken.location.proc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.items.Item;
import com.eldritch.invoken.location.ConnectedRoom;
import com.eldritch.invoken.location.ConnectedRoomManager;
import com.eldritch.invoken.location.NaturalVector2;
import com.eldritch.invoken.proto.Actors.Container;
import com.eldritch.invoken.proto.Actors.InventoryItem;
import com.eldritch.invoken.proto.Actors.PlayerActor;
import com.eldritch.invoken.state.Inventory;
import com.eldritch.invoken.util.Constants;
import com.google.common.base.Optional;

/**
 * Generates loot to populate containers based on a number of criteria.
 * 
 * TODO: when the player respawns, the totalValue here should carry over so we do not keep giving
 * them lots of resources.
 */
public class LootGenerator {
    private static final int RARE_ITEM_VALUE = 300;
    private static final int MAX_ITEM_VALUE = 500;
    private static final int MINOR_VALUE = 100;

    private final int floor;
    private final Optional<PlayerActor> state;
    private final ConnectedRoomManager rooms;

    private final List<InventoryItem> items = new ArrayList<>();
    private final Random rand = new Random(); // don't seed the RNG for different loot between loads

    private final int totalTargetValue;
    private int totalValue = 0;
    private int rareItemCount = 0;

    public LootGenerator(int floor, Optional<PlayerActor> state, ConnectedRoomManager rooms) {
        this.floor = floor;
        this.state = state;
        this.rooms = rooms;

        Container container = Inventory.getContainer(Constants.RANDOM_LOOT);
        items.addAll(container.getItemList());

        this.totalTargetValue = (int) (floor * 25 + Math.log(floor + 3) * 150 + 250);
    }

    public Inventory generate(NaturalVector2 position) {
        int targetValue = (int) (rand.nextDouble() * (totalTargetValue - totalValue));
        targetValue = Math.max(targetValue - totalValue, MINOR_VALUE);
        int currentValue = 0;

        Inventory inv = new Inventory();
        Collections.shuffle(items);
        for (InventoryItem proto : items) {
            // check our current value
            if (currentValue >= targetValue) {
                break;
            }

            Item item = Item.fromProto(InvokenGame.ITEM_READER.readAsset(proto.getItemId()));
            if (isTooValuable(item)) {
                // some items should be saved for lower floors to prevent the player from
                // becoming overpowered
                continue;
            }

            if (isRare(item)) {
                InvokenGame.log("rare item: " + item.getName() + " value: " + item.getValue());

                // rare items are handled differently
                // instead of placing with some prob, we place one per floor
                // as floor increases, previously rare items become commonplace
                if (rareItemCount == 0 && !onCriticalPath(position)) {
                    // we want to add some quantity of rare items to each floor, off the beaten path
                    // to encourage exploration and discovery
                    inv.addItem(item);
                    currentValue += item.getValue();
                    rareItemCount++;
                    InvokenGame.log("placement: " + position);
                }
                continue;
            }

            int count = Inventory.getDesiredCount(proto, rand);
            if (count > 0) {
                InvokenGame.log("common item: " + item.getName());

                int addedValue = item.getValue() * count;
                if (currentValue + addedValue > targetValue) {
                    // reduce count to barely fit
                    count = (targetValue - currentValue) / item.getValue();
                    addedValue = item.getValue() * count;
                }

                if (count > 0) {
                    // go ahead and add all the items
                    inv.addItem(item, count);
                    currentValue += addedValue;
                }
            }
        }

        totalValue += currentValue;
        return inv;
    }

    private boolean isTooValuable(Item item) {
        int threshold = 100 * floor + MAX_ITEM_VALUE;
        return item.getValue() > threshold;
    }

    private boolean isRare(Item item) {
        int rareThreshold = 100 * floor + RARE_ITEM_VALUE;
        return item.getValue() >= rareThreshold;
    }

    private boolean onCriticalPath(NaturalVector2 position) {
        if (!rooms.hasRoom(position.x, position.y)) {
            return false;
        }

        ConnectedRoom room = rooms.getRoom(position.x, position.y);
        return room.onCriticalPath();
    }
}
