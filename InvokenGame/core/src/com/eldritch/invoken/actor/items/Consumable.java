package com.eldritch.invoken.actor.items;

import java.util.List;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.eldritch.invoken.actor.AgentInventory;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.actor.type.Agent.Direction;
import com.eldritch.invoken.effects.Effect;
import com.eldritch.invoken.effects.EffectFactory;
import com.eldritch.invoken.effects.EffectFactory.EffectGenerator;
import com.eldritch.invoken.proto.Items;

public class Consumable extends Item {
    private final List<EffectGenerator> effects;
    
    public Consumable(Items.Item data) {
        super(data, 0);
        effects = EffectFactory.from(data.getEffectList());
    }

    @Override
    public boolean isEquipped(AgentInventory inventory) {
        // cannot be equipped
        return false;
    }
    
    @Override
    public void equipFrom(AgentInventory inventory) {
        // consume
        inventory.removeItem(this);
        apply(inventory.getAgentInfo().getAgent());
    }

    @Override
    public void unequipFrom(AgentInventory inventory) {
        // does nothing
    }
    
    @Override
    public boolean mapTo(AgentInventory inventory, int index) {
        inventory.setConsumable(index, this);
        return true;
    }

    @Override
    protected Animation getAnimation(Activity activity, Direction direction) {
        // not animated
        return null;
    }

    private void apply(Agent target) {
        for (EffectGenerator generator : effects) {
            Effect effect = generator.generate(target);
            target.addEffect(effect);
        }
    }
}
