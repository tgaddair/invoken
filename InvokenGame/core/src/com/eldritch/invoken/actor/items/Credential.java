package com.eldritch.invoken.actor.items;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.eldritch.invoken.actor.AgentInventory;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.actor.type.Agent.Direction;
import com.eldritch.invoken.proto.Items;
import com.eldritch.invoken.proto.Locations.Room;
import com.google.common.base.Strings;

public class Credential extends Item {
    public Credential(Items.Item data) {
        super(data, 0);
    }

    @Override
    public boolean isEquipped(AgentInventory inventory) {
        // cannot be equipped
        return false;
    }

    @Override
    public void equipFrom(AgentInventory inventory) {
    }

    @Override
    public void unequipFrom(AgentInventory inventory) {
    }

    @Override
    protected Animation getAnimation(Activity activity, Direction direction) {
        // not animated
        return null;
    }

    public static Credential from(int id, Room data) {
        Items.Item.Builder builder = Items.Item.newBuilder();
        builder.setId(String.valueOf(id));
        builder.setName(!Strings.isNullOrEmpty(data.getId()) ? data.getId() : String.valueOf(id));
        return new Credential(builder.build());
    }
}
