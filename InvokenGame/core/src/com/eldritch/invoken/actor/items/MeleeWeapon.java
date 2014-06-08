package com.eldritch.invoken.actor.items;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.eldritch.invoken.actor.Inventory;
import com.eldritch.invoken.actor.Agent.Activity;
import com.eldritch.invoken.actor.Agent.Direction;

public class MeleeWeapon extends Item {
    public MeleeWeapon(com.eldritch.scifirpg.proto.Items.Item item) {
        super(item, 0);
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
}
