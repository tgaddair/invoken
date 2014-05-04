package com.eldritch.invoken.actor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.items.Item;
import com.eldritch.invoken.actor.items.Outfit;
import com.eldritch.invoken.actor.items.RangedWeapon;
import com.eldritch.scifirpg.proto.Actors.ActorParams.InventoryItem;

public class Inventory {
	private final Map<String, ItemState> items = new HashMap<String, Inventory.ItemState>();
	
	// equipment
	private Outfit outfit;
	private RangedWeapon rangedWeapon;
	
	public boolean hasOutfit() {
		return outfit != null;
	}
	
	public Outfit getOutfit() {
		return outfit;
	}
	
	public void setOutfit(Outfit outfit) {
		this.outfit = outfit;
	}
	
	public boolean hasWeapon() {
		return rangedWeapon != null;
	}
	
	public RangedWeapon getWeapon() {
		return rangedWeapon;
	}
	
	public void setWeapon(RangedWeapon weapon) {
		this.rangedWeapon = weapon;
	}
	
	public void add(InventoryItem item) {
		items.put(item.getItemId(), new Inventory.ItemState(item));
	}
	
	public Collection<ItemState> getItems() {
		return items.values();
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
	
	public void equip(Item item) {
		item.equipFrom(this);
	}
	
	public void addItem(Item item) {
		addItem(item, 1);
	}
	
	public void addItem(Item item, int count) {
        if (!items.containsKey(item.getId())) {
        	items.put(item.getId(), new Inventory.ItemState(item, count));
        } else {
        	items.get(item.getId()).add(count);
        }
    }
	
	public void removeItem(Item item) {
		removeItem(item.getId(), 1);
	}
	
	/**
     * Remove the requested number of instances of the given item from the
     * actor's inventory. If the number requested is greater than or equal to
     * the number available, or if count == -1, then we remove all and unequip.
     */
    public void removeItem(String itemId, int count) {
        int available = getItemCount(itemId);
        if (available == 0) {
            // nothing to remove
            return;
        }

        if (count >= available || count == -1) {
            // remove all and unequip
            Item item = items.get(itemId).getItem();
            item.unequipFrom(this);
        	items.remove(itemId);
        } else {
            // decrement counters
        	items.get(itemId).remove(count);
        }
    }
	
	public static class ItemState {
	    private final Item item;
	    private int count;
	
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
	
	    public void add(int c) {
	        count += c;
	    }
	
	    public void remove(int c) {
	        // Can't have negative count
	        count = Math.max(count - c, 0);
	    }
	
	    public int getCount() {
	        return count;
	    }
	}
}
