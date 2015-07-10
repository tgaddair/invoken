package com.eldritch.invoken.actor.items;

import java.util.List;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.AgentInventory;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.actor.type.Agent.Direction;
import com.eldritch.invoken.effects.Effect;
import com.eldritch.invoken.effects.EffectFactory;
import com.eldritch.invoken.effects.EffectFactory.EffectGenerator;
import com.eldritch.invoken.proto.Items;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.util.SoundManager.SoundEffect;

public class Consumable extends Item {
    private final List<EffectGenerator> effects;
    private final Texture icon;

    public Consumable(Items.Item data) {
        super(data, 0);
        effects = EffectFactory.from(data.getEffectList());
        String asset = data.hasAsset() ? data.getAsset() : "default";
        this.icon = GameScreen.getTexture("icon/consumable/" + asset + ".png");
    }

    public Texture getIcon() {
        return icon;
    }

    @Override
    public void equipIfBetter(AgentInventory inventory) {
        // do not equip
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
        // consume
        inventory.removeItem(this);
        apply(inventory.getAgentInfo().getAgent());
        InvokenGame.SOUND_MANAGER.play(SoundEffect.CONSUMABLE, 3);
    }

    @Override
    public void unequipFrom(AgentInventory inventory) {
        // does nothing
    }

    @Override
    public boolean mapTo(AgentInventory inventory, int index) {
        if (inventory.getAgentInfo().getAgent().canEquip(this)) {
            inventory.setConsumable(index, this);
            return true;
        }
        return false;
    }
    
    @Override
    public boolean isEncrypted() {
        return true;
    }

    @Override
    protected Animation getAnimation(Activity activity, Direction direction) {
        // not animated
        return null;
    }
    
    @Override
    public String getTypeName() {
        return "Consumable";
    }

    private void apply(Agent target) {
        for (EffectGenerator generator : effects) {
            Effect effect = generator.generate(target);
            target.addEffect(effect);
        }
    }
}
