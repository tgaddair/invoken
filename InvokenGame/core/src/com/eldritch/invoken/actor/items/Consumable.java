package com.eldritch.invoken.actor.items;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.eldritch.invoken.actor.AgentInventory;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.actor.type.Agent.Direction;
import com.eldritch.invoken.effects.Effect;
import com.eldritch.invoken.effects.EffectFactory;
import com.eldritch.invoken.proto.Effects;
import com.eldritch.invoken.proto.Items;

public class Consumable extends Item {
    public Consumable(Items.Item data) {
        super(data, 0);
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
    protected Animation getAnimation(Activity activity, Direction direction) {
        // not animated
        return null;
    }

    private void apply(Agent target) {
        for (Effects.Effect proto : getData().getEffectList()) {
            Effect effect = EffectFactory.from(proto).generate(target);
            target.addEffect(effect);
        }
    }
}
