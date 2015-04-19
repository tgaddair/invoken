package com.eldritch.invoken.actor;

import java.util.ArrayList;

import com.eldritch.invoken.actor.items.Item;
import com.eldritch.invoken.actor.items.MeleeWeapon;
import com.eldritch.invoken.actor.items.Outfit;
import com.eldritch.invoken.actor.items.RangedWeapon;
import com.eldritch.invoken.proto.Actors.InventoryItem;
import com.eldritch.invoken.state.Inventory;

public class AgentInventory extends Inventory {
    // equipment
    private Outfit outfit;
    private RangedWeapon rangedWeapon;
    private MeleeWeapon meleeWeapon;
    
    public AgentInventory() {
        super(new ArrayList<InventoryItem>());
    }
    
    public void update(float delta) {
        if (hasRangedWeapon()) {
            ItemState state = getState(rangedWeapon.getId());
            if (state.getCooldown() > 0) {
                state.cooldown(delta);
            }
        }
    }
    
    public boolean canUseRangedWeapon() {
        return hasRangedWeapon() && !isCooling(rangedWeapon);
    }
    
    public void setCooldown(Item item, float cooldown) {
        getState(item.getId()).setCooldown(cooldown);
    }
    
    public boolean isCooling(Item item) {
        return getState(item.getId()).getCooldown() > 0;
    }

    public boolean hasOutfit() {
        return outfit != null;
    }

    public Outfit getOutfit() {
        return outfit;
    }

    public void setOutfit(Outfit outfit) {
        this.outfit = outfit;
    }

    public boolean hasRangedWeapon() {
        return rangedWeapon != null;
    }

    public RangedWeapon getRangedWeapon() {
        return rangedWeapon;
    }

    public void setRangedWeapon(RangedWeapon weapon) {
        this.rangedWeapon = weapon;
    }

    public boolean hasMeleeWeapon() {
        return meleeWeapon != null;
    }

    public MeleeWeapon getMeleeWeapon() {
        return meleeWeapon;
    }

    public void setMeleeWeapon(MeleeWeapon weapon) {
        this.meleeWeapon = weapon;
    }
    
    public void equip(Item item) {
        item.equipFrom(this);
    }

    public void unequip(Item item) {
        item.unequipFrom(this);
    }
    
    @Override
    protected void handleRemove(Item item) {
        item.unequipFrom(this);
        super.handleRemove(item);
    }
}
