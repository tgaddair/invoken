package com.eldritch.invoken.actor.items;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.Inventory;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.actor.type.Agent.Direction;
import com.eldritch.invoken.proto.Items;

public class Fragment extends Item {
    private static Fragment instance = null;
    
    private Fragment(Items.Item data) {
        // singleton
        super(data, 0);
    }

    @Override
    public boolean isEquipped(Inventory inventory) {
        // cannot be equipped
        return false;
    }

    @Override
    public void equipFrom(Inventory inventory) {
    }

    @Override
    public void unequipFrom(Inventory inventory) {
    }

    @Override
    protected Animation getAnimation(Activity activity, Direction direction) {
        // not animated
        return null;
    }
    
    public static Fragment getInstance() {
        return getInstance(InvokenGame.ITEM_READER.readAsset("Fragment"));
    }
    
    public static Fragment getInstance(Items.Item data) {
        if (instance == null) {
            instance = new Fragment(data);
        }
        return instance;
    }
}
