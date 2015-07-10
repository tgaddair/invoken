package com.eldritch.invoken.actor.items;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.AgentInventory;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.actor.type.Agent.Direction;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.proto.Items;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class Icepik extends Item {
    private static final LoadingCache<Integer, Icepik> piksByLevel = CacheBuilder.newBuilder()
            .build(new CacheLoader<Integer, Icepik>() {
                public Icepik load(Integer level) {
                    String id = "Icepik" + level;
                    Items.Item data;
                    if (InvokenGame.ITEM_READER.hasAsset(id)) {
                        data = InvokenGame.ITEM_READER.readAsset(id);
                    } else {
                        Items.Item.Builder builder = Items.Item.newBuilder();
                        builder.setId(id);
                        builder.setName("Icepik " + level);
                        builder.setType(Items.Item.Type.CREDENTIAL);
                        builder.setDescription("");
                        builder.setValue(0);
                        builder.setDroppable(true);
                        data = builder.build();
                    }
                    return new Icepik(data, level);
                }
            });
    
    private final int level;

    private Icepik(Items.Item data, int level) {
        super(data, 0);
        this.level = level;
    }
    
    public int getLevel() {
        return level;
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
    
    @Override
    public String getTypeName() {
        return "Icepik";
    }
    
    public static Icepik from(Items.Item data) {
        int strength = Integer.valueOf(data.getId().replaceAll("^[A-Za-z]+", ""));
        Icepik pik = piksByLevel.getIfPresent(strength);
        if (pik == null) {
            pik = new Icepik(data, strength);
            piksByLevel.put(strength, pik);
        }
        return pik;
    }

    public static Icepik from(Level level) {
        return from(level.getFloor() / 10 + 1);
    }
    
    public static Icepik from(int level) {
        try {
            return piksByLevel.get(level);
        } catch (Exception ex) {
            InvokenGame.error("Failed to load icepik: " + level, ex);
            return null;
        }
    }
}
