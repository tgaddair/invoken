package com.eldritch.invoken.state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.items.Item;
import com.eldritch.invoken.proto.Actors.Container;
import com.eldritch.invoken.proto.Actors.InventoryItem;
import com.eldritch.invoken.proto.Items.Item.Type;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

public class Inventory {
    private static final LoadingCache<String, Container> CONTAINER_LOADER = CacheBuilder
            .newBuilder().build(new CacheLoader<String, Container>() {
                public Container load(String id) {
                    return InvokenGame.CONTAINER_READER.readAsset(id);
                }
            });

    private final Map<Item, InventoryItem> itemMap = new HashMap<>();
    private final Map<String, ItemState> items = new HashMap<>();
    private final Random rand = new Random();

    public Inventory() {
        this(ImmutableList.<InventoryItem> of());
    }

    public Inventory(List<InventoryItem> items) {
        for (InventoryItem item : items) {
            add(item);
        }
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public void add(InventoryItem proto) {
        if (checkForDrop(proto)) {
            addItem(proto);
        }
    }

    protected boolean checkForDrop(Item item) {
        if (itemMap.containsKey(item)) {
            return checkForDrop(itemMap.get(item));
        }

        // when the item wasn't added with a drop chance from a specification, then it was
        // acquired dynamically, so we should always drop it
        return true;
    }

    protected boolean checkForDrop(InventoryItem proto) {
        // possibly add some number of the given item
        boolean hasItem = true;
        if (proto.getDropChance() < 1) {
            // roll to see if we have any of the item in this inventory
            hasItem = rand.nextDouble() < proto.getDropChance();
        }
        return hasItem;
    }

    protected double getDropChance(Item item) {
        if (itemMap.containsKey(item)) {
            return itemMap.get(item).getDropChance();
        }
        return 1.0;
    }

    protected final double random() {
        return rand.nextDouble();
    }

    protected void addItem(InventoryItem proto) {
        // add plus or minus a fraction of the possible variation
        int count = proto.getCount();
        if (proto.getVariance() > 0) {
            int delta = (int) (rand.nextFloat() * 2 * proto.getVariance() - proto.getVariance());
            count += delta;
        }

        // add the item if we end up with a positive count
        if (count > 0) {
            Item item = Item.fromProto(InvokenGame.ITEM_READER.readAsset(proto.getItemId()));
            itemMap.put(item, proto);
            addItem(item, count);
        }
    }

    public boolean hasItem(Item item) {
        return getItemCount(item) > 0;
    }

    public int getItemCount(Item item) {
        return getItemCount(item.getId());
    }

    public int getItemCount(String itemId) {
        if (!items.containsKey(itemId)) {
            return 0;
        }
        return items.get(itemId).getCount();
    }

    public boolean hasItem(String id) {
        return items.containsKey(id);
    }

    public Item getItem(String id) {
        return items.get(id).item;
    }

    protected ItemState getState(String id) {
        return items.get(id);
    }

    public Iterable<ItemState> getItems() {
        return items.values();
    }

    /**
     * Iterating over this newly constructed collection will allow the caller to remove items from
     * the inventory without concurrent modification.
     */
    public Iterable<Item> getStatelessItems() {
        List<Item> statelessItems = new ArrayList<>();
        for (ItemState state : getItems()) {
            statelessItems.add(state.getItem());
        }
        return statelessItems;
    }

    public Map<Item, Integer> getItemCounts(Type type) {
        Map<Item, Integer> map = Maps.newHashMap();
        for (ItemState item : items.values()) {
            if (item.item.getData().getType() == type) {
                map.put(item.item, item.count);
            }
        }
        return map;
    }

    public void addItem(Item item) {
        addItem(item, 1);
    }

    public void addItem(Item item, int count) {
        if (count <= 0) {
            // disallow this
            return;
        }

        if (!items.containsKey(item.getId())) {
            items.put(item.getId(), new Inventory.ItemState(item, count));
        } else {
            items.get(item.getId()).add(count);
        }
        onAdd(item, count);
    }

    protected void onAdd(Item item, int count) {
    }

    public void removeItem(Item item) {
        removeItem(item.getId(), 1);
    }

    public int removeItem(Item item, int count) {
        return removeItem(item.getId(), count);
    }

    /**
     * Remove the requested number of instances of the given item from the actor's inventory. If the
     * number requested is greater than or equal to the number available, or if count == -1, then we
     * remove all and unequip.
     * 
     * Returns the number of items actually removed.
     */
    public int removeItem(String itemId, int count) {
        int available = getItemCount(itemId);
        if (available == 0) {
            // nothing to remove
            return 0;
        }

        ItemState state = items.get(itemId);
        int removed;
        if (count >= available || count == -1) {
            // remove all and unequip
            handleRemove(state.getItem());
            removed = available;
        } else {
            // decrement counters
            state.remove(count);
            removed = count;
        }

        onRemove(state.getItem(), removed);
        return removed;
    }

    protected void onRemove(Item item, int count) {
    }

    protected void handleRemove(Item item) {
        items.remove(item.getId());
    }

    public static class ItemState {
        private final Item item;
        private int count;
        private float cooldown = 0;

        public ItemState(Item item, int count) {
            this.item = item;
            this.count = count;
        }

        public ItemState(InventoryItem item) {
            this.item = Item.fromProto(InvokenGame.ITEM_READER.readAsset(item.getItemId()));
            count = item.getCount();
        }

        public Item getItem() {
            return item;
        }

        private void add(int c) {
            count += c;
        }

        private void remove(int c) {
            // Can't have negative count
            count = Math.max(count - c, 0);
        }

        public int getCount() {
            return count;
        }

        public void setCooldown(float cooldown) {
            this.cooldown = cooldown;
        }

        public float getCooldown() {
            return cooldown;
        }

        public void cooldown(float delta) {
            cooldown -= delta;
        }

        public InventoryItem toProto() {
            return InventoryItem.newBuilder().setItemId(item.getId()).setCount(count).build();
        }
    }

    public static Inventory from(String id) {
        return new Inventory(getContainer(id).getItemList());
    }

    public static Container getContainer(String id) {
        try {
            return CONTAINER_LOADER.get(id);
        } catch (ExecutionException ex) {
            InvokenGame.error("Failed to load container: " + id, ex);
            return null;
        }
    }
}
