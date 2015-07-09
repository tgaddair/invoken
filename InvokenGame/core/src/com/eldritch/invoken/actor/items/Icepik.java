package com.eldritch.invoken.actor.items;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.AgentInventory;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.actor.type.Agent.Direction;
import com.eldritch.invoken.proto.Items;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class Icepik extends Item {
    private static final LoadingCache<Integer, Icepik> piksByStrength = CacheBuilder.newBuilder()
            .build(new CacheLoader<Integer, Icepik>() {
                public Icepik load(Integer strength) {
                    Items.Item.Builder builder = Items.Item.newBuilder();
                    builder.setId("Icepik" + strength);
                    builder.setName("Icepik " + strength);
                    builder.setType(Items.Item.Type.CREDENTIAL);
                    builder.setDescription("");
                    builder.setValue(0);
                    builder.setDroppable(true);
                    return new Icepik(builder.build(), strength);
                }
            });
    
    private final int strength;

    private Icepik(Items.Item data, int strength) {
        super(data, 0);
        this.strength = strength;
    }
    
    public int getStrength() {
        return strength;
    }

    @Override
    public boolean isEquipped(AgentInventory inventory) {
        // cannot be equipped
        return false;
    }

    @Override
    public void addFrom(AgentInventory inventory) {
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

    public static Icepik from(int strength) {
        try {
            return piksByStrength.get(strength);
        } catch (Exception ex) {
            InvokenGame.error("Failed to load icepik: " + strength, ex);
            return null;
        }
    }
}
