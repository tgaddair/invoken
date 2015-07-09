package com.eldritch.invoken.actor.items;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.AgentInventory;
import com.eldritch.invoken.actor.type.Agent;
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
                    String id = "Icepik" + strength;
                    Items.Item data;
                    if (InvokenGame.ITEM_READER.hasAsset(id)) {
                        data = InvokenGame.ITEM_READER.readAsset(id);
                    } else {
                        Items.Item.Builder builder = Items.Item.newBuilder();
                        builder.setId(id);
                        builder.setName("Icepik " + strength);
                        builder.setType(Items.Item.Type.CREDENTIAL);
                        builder.setDescription("");
                        builder.setValue(0);
                        builder.setDroppable(true);
                        data = builder.build();
                    }
                    return new Icepik(data, strength);
                }
            });
    
    private final int strength;

    private Icepik(Items.Item data, int strength) {
        super(data, 0);
        this.strength = strength;
    }
    
    public int getRequiredCount(Agent agent) {
        int ability = agent.getInfo().getSubterfuge() / 10;
        int difficulty = strength;
        if (ability >= difficulty) {
            return 1;
        }
        
        // required piks scales with the square of the difference
        int delta = difficulty - ability + 1;
        return delta * delta;
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
    
    public static Icepik from(Items.Item data) {
        int strength = Integer.valueOf(data.getId().replaceAll("^[A-Za-z]+", ""));
        Icepik pik = piksByStrength.getIfPresent(strength);
        if (pik == null) {
            pik = new Icepik(data, strength);
            piksByStrength.put(strength, pik);
        }
        return pik;
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
