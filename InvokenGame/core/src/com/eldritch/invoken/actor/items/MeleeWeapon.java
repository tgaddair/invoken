package com.eldritch.invoken.actor.items;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.eldritch.invoken.actor.AgentInventory;
import com.eldritch.invoken.actor.type.Human;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.actor.type.Agent.Direction;
import com.eldritch.invoken.proto.Items;
import com.eldritch.invoken.proto.Items.Item.DamageMod;
import com.eldritch.invoken.util.AnimationUtils;
import com.eldritch.invoken.util.Settings;
import com.google.common.base.Strings;

public class MeleeWeapon extends Item {
    private final Map<Direction, Animation> animations = new HashMap<Direction, Animation>();
    private final float damage;
    private final float range;
    private final boolean visible;
    
    private MeleeWeapon(Items.Item item, Map<Direction, Animation> animations, float width, float height) {
        super(item, width, height);
        this.animations.putAll(animations);

        // calculate damage magnitude
        float damageSum = 0;
        for (DamageMod mod : item.getDamageModifierList()) {
            damageSum += mod.getMagnitude();
        }
        this.damage = damageSum;
        this.range = (float) item.getRange();
        this.visible = !item.getHidden();
    }

    @Override
    public boolean isEquipped(AgentInventory inventory) {
        return inventory.getMeleeWeapon() == this;
    }
    
    @Override
    public void addFrom(AgentInventory inventory) {
    }

    @Override
    public void equipFrom(AgentInventory inventory) {
        inventory.setMeleeWeapon(this);
    }

    @Override
    public void unequipFrom(AgentInventory inventory) {
        if (inventory.getMeleeWeapon() == this) {
            inventory.setMeleeWeapon(null);
        }
    }
    
    @Override
    public boolean isEncrypted() {
        return true;
    }

    @Override
    protected Animation getAnimation(Activity activity, Direction direction) {
        return animations.get(direction);
    }
    
    @Override
    public String getTypeName() {
        return "Melee Weapon";
    }

    public float getRange() {
        return range;
    }

    public float getDamage() {
        return damage;
    }

    public boolean isVisible() {
        return visible;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(String.format("%s\n" + "Range: %.2f\n",
                super.toString(), data.getRange()));
        result.append("Damage:");
        for (DamageMod mod : data.getDamageModifierList()) {
            result.append(String.format("\n  %s: %d", mod.getDamage(), mod.getMagnitude()));
        }
        return result.toString();
    }
    
    public static MeleeWeapon from(Items.Item item) {
        Map<Direction, Animation> animations = new HashMap<Direction, Animation>();
        float width = 0;
        float height = 0;
        if (!Strings.isNullOrEmpty(item.getAsset())) {
            if (item.getAsset().contains("full")) {
                animations.putAll(AnimationUtils.getHumanAnimations(item.getAsset()).get(
                        Activity.Swipe));
                width = Human.getWidth();
                height = Human.getHeight();
            } else {
                final int size = 192;
                animations.putAll(Human.getAnimations(item.getAsset(), size));
                width = size * Settings.SCALE;
                height = size * Settings.SCALE;
            }
        }
        
        return new MeleeWeapon(item, animations, width, height);
    }
}
