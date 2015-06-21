package com.eldritch.invoken.actor;

import java.util.ArrayList;

import com.eldritch.invoken.actor.items.Ammunition;
import com.eldritch.invoken.actor.items.Consumable;
import com.eldritch.invoken.actor.items.Item;
import com.eldritch.invoken.actor.items.MeleeWeapon;
import com.eldritch.invoken.actor.items.Outfit;
import com.eldritch.invoken.actor.items.RangedWeapon;
import com.eldritch.invoken.proto.Actors.InventoryItem;
import com.eldritch.invoken.state.Inventory;

public class AgentInventory extends Inventory {
    private final AgentInfo info;

    // equipment
    private final Consumable[] consumables = new Consumable[2];
    private Outfit outfit;
    private RangedWeapon rangedWeapon;
    private MeleeWeapon meleeWeapon;
    private Ammunition ammunition;

    public AgentInventory(AgentInfo info) {
        super(new ArrayList<InventoryItem>());
        this.info = info;
    }

    public AgentInfo getAgentInfo() {
        return info;
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
        // return hasRangedWeapon() && !isCooling(rangedWeapon);
        return hasRangedWeapon() && getAmmunitionCount() > 0;
    }

    public void setCooldown(Item item, float cooldown) {
        getState(item.getId()).setCooldown(cooldown);
    }

    public boolean isCooling(Item item) {
        return getState(item.getId()).getCooldown() > 0;
    }

    public boolean hasConsumable(int index) {
        return index < consumables.length && consumables[index] != null;
    }

    public Consumable getConsumable(int index) {
        return consumables[index];
    }

    public void setConsumable(int index, Consumable consumable) {
        consumables[index] = consumable;
    }

    public boolean canConsume(int index) {
        return canConsume(getConsumable(index));
    }

    public boolean canConsume(Consumable consumable) {
        return consumable != null && hasItem(consumable);
    }

    public boolean consume(int index) {
        return consume(consumables[index]);
    }

    public boolean consume(Consumable consumable) {
        if (canConsume(consumable)) {
            consumable.equipFrom(this);
            return true;
        }
        return false;
    }

    public boolean hasOutfit() {
        return outfit != null;
    }

    public Outfit getOutfit() {
        return outfit;
    }

    public void setOutfit(Outfit outfit) {
        if (this.outfit != null) {
            info.getAgent().addVelocityPenalty(-this.outfit.getWeight());
        }
        this.outfit = outfit;
        if (outfit != null) {
            info.getAgent().addVelocityPenalty(outfit.getWeight());
        }
    }

    public boolean hasRangedWeapon() {
        return rangedWeapon != null;
    }
    
    public void useAmmunition(int x) {
        if (ammunition != null) {
            getState(ammunition.getId()).remove(x);
        }
    }

    public int getAmmunitionCount() {
        if (ammunition == null) {
            return 0;
        }
        return getState(ammunition.getId()).getCount();
    }

    public RangedWeapon getRangedWeapon() {
        return rangedWeapon;
    }

    public void setRangedWeapon(RangedWeapon weapon) {
        this.rangedWeapon = weapon;
        if (weapon != null) {
            for (ItemState state : getItems()) {
                if (state.getItem() instanceof Ammunition) {
                    Ammunition current = (Ammunition) state.getItem();
                    if (current.getType() == weapon.getType()) {
                        this.ammunition = current;
                    }
                }
            }
        } else {
            ammunition = null;
        }
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

    public void equipIfBetter(Item item) {
        item.equipIfBetter(this);
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
