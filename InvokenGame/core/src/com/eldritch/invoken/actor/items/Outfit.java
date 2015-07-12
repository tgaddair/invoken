package com.eldritch.invoken.actor.items;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.eldritch.invoken.actor.AgentInventory;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.actor.type.Agent.Direction;
import com.eldritch.invoken.actor.type.Human;
import com.eldritch.invoken.proto.Effects.DamageType;
import com.eldritch.invoken.proto.Items.Item.DamageMod;
import com.eldritch.invoken.util.AnimationUtils;

public class Outfit extends Item {
    private final Map<DamageType, Integer> resistance = new HashMap<>();
    private final Map<Activity, Map<Direction, Animation>> animations;
    private final float defense; // percentage

    public Outfit(com.eldritch.invoken.proto.Items.Item item) {
        super(item, Human.PX);
        animations = AnimationUtils.getHumanAnimations(getAssetPath(item.getAsset()));
        
        float damageSum = 0;
        for (DamageMod mod : item.getDamageModifierList()) {
            damageSum += mod.getMagnitude();
            resistance.put(mod.getDamage(), mod.getMagnitude());
        }
        defense = damageSum / 100;
    }

    public boolean covers() {
        return getData().getCovers();
    }
    
    public float getDefense(DamageType type) {
        if (!resistance.containsKey(type)) {
            return 0;
        }
        return resistance.get(type);
    }
    
    public float getDefense() {
        return defense;
    }
    
    public float getWeightPenalty() {
        return (float) getWeight() / 25f;
    }
    
    @Override
    public boolean isEquipped(AgentInventory inventory) {
        return inventory.getOutfit() == this;
    }
    
    @Override
    public void addFrom(AgentInventory inventory) {
    }

    @Override
    public void equipFrom(AgentInventory inventory) {
        inventory.setOutfit(this);
    }

    @Override
    public void unequipFrom(AgentInventory inventory) {
        if (inventory.getOutfit() == this) {
            inventory.setOutfit(null);
        }
    }
    
    @Override
    public boolean isEncrypted() {
        return true;
    }

    @Override
    protected Animation getAnimation(Activity activity, Direction direction) {
        return animations.get(activity).get(direction);
    }
    
    @Override
    public String getTypeName() {
        return "Outfit";
    }

    private static String getAssetPath(String basename) {
        return String.format("sprite/items/outfits/%s.png", basename);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(String.format("%s\n", super.toString()));
        result.append("Resistance:");
        for (DamageMod mod : data.getDamageModifierList()) {
            result.append(String.format("\n  %s: %d", mod.getDamage(), mod.getMagnitude()));
        }
        return result.toString();
    }
}
