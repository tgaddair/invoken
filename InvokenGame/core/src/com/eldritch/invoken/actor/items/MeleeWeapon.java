package com.eldritch.invoken.actor.items;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.eldritch.invoken.actor.Inventory;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.actor.type.Agent.Direction;
import com.eldritch.scifirpg.proto.Items.Item.DamageMod;

public class MeleeWeapon extends Item {
    private final float damage;
    private final float range;
    
    public MeleeWeapon(com.eldritch.scifirpg.proto.Items.Item item) {
        super(item, 0);
        
        // calculate damage magnitude
        float damageSum = 0;
        for (DamageMod mod : item.getDamageModifierList()) {
            damageSum += mod.getMagnitude();
        }
        this.damage = damageSum;
        this.range = (float) item.getRange();
    }
    
    @Override
    public boolean isEquipped(Inventory inventory) {
        return inventory.getMeleeWeapon() == this;
    }
    
    @Override
    public void equipFrom(Inventory inventory) {
        inventory.setMeleeWeapon(this);
    }
    
    @Override
    public void unequipFrom(Inventory inventory) {
        if (inventory.getMeleeWeapon() == this) {
            inventory.setMeleeWeapon(null);
        }
    }
    
    @Override
    protected Animation getAnimation(Activity activity, Direction direction) {
        return null;
    }
    
    public float getRange() {
        return range;
    }
    
    public float getDamage() {
        return damage;
    }
}
