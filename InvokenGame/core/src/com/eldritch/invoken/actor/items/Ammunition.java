package com.eldritch.invoken.actor.items;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.eldritch.invoken.actor.AgentInventory;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.actor.type.Agent.Direction;
import com.eldritch.invoken.proto.Items;
import com.eldritch.invoken.proto.Items.Item.RangedWeaponType;

public class Ammunition extends Item {
    public Ammunition(Items.Item data) {
        super(data, 0);
    }
    
    public RangedWeaponType getType() {
        return data.getRangedType();
    }

    @Override
    public boolean isEquipped(AgentInventory inventory) {
        RangedWeaponType type = getType();
        return inventory.hasAmmunition(type) && inventory.getAmmunition(type) == this;
    }
    
    @Override
    public void addFrom(AgentInventory inventory) {
        if (!inventory.hasAmmunition(getType())) {
            equipFrom(inventory);
        }
    }

    @Override
    public void equipFrom(AgentInventory inventory) {
        inventory.setAmmunition(getType(), this);
    }

    @Override
    public void unequipFrom(AgentInventory inventory) {
        inventory.removeAmmunition(getType());
    }

    @Override
    protected Animation getAnimation(Activity activity, Direction direction) {
        // not animated
        return null;
    }
    
    @Override
    public String getTypeName() {
        return "Ammunition";
    }
}
