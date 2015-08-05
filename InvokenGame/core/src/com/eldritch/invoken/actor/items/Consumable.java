package com.eldritch.invoken.actor.items;

import java.util.List;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.AgentInventory;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.actor.type.Agent.Direction;
import com.eldritch.invoken.actor.type.Collectible;
import com.eldritch.invoken.actor.type.Collectible.CollectibleGenerator;
import com.eldritch.invoken.effects.Effect;
import com.eldritch.invoken.effects.EffectFactory;
import com.eldritch.invoken.effects.EffectFactory.EffectGenerator;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.proto.Items;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.state.Inventory;
import com.eldritch.invoken.util.SoundManager.SoundEffect;
import com.google.common.base.Optional;

public class Consumable extends Item {
    private static final float CONSUMABLE_SCALE = 0.5f;

    private final List<EffectGenerator> effects;
    private final Texture icon;
    private final Optional<ConsumableGenerator> generator;

    public Consumable(Items.Item data) {
        super(data, 0);
        effects = EffectFactory.from(this, data.getEffectList());
        String asset = data.hasAsset() ? data.getAsset() : "default";
        this.icon = GameScreen.getTexture("icon/consumable/" + asset + ".png");

        TextureRegion collectibleRegion = getRegion(data.getAsset());
        this.generator = collectibleRegion != null ? Optional.of(new ConsumableGenerator(
                collectibleRegion)) : Optional.<ConsumableGenerator> absent();
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
    public void releaseFrom(Inventory inventory, Level level, Vector2 position) {
        if (generator.isPresent()) {
            int total = inventory.getItemCount((this));
            generator.get().release(level, position, total);
            inventory.removeItem(this, total);
        }
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

    private static class ConsumableEntity extends Collectible {
        public ConsumableEntity(Consumable item, TextureRegion region, Vector2 origin,
                Vector2 direction, int quantity, float r) {
            super(item, quantity, region, origin, direction, r);
        }

        @Override
        protected void onCollect(Agent agent) {
            // TODO: popup for collectibles
        }
    }

    private class ConsumableGenerator extends CollectibleGenerator<ConsumableEntity> {
        private final TextureRegion region;

        public ConsumableGenerator(TextureRegion region) {
            super(CONSUMABLE_SCALE);
            this.region = region;
        }

        @Override
        protected ConsumableEntity generate(Vector2 origin, Vector2 direction, int quantity, float r) {
            return new ConsumableEntity(Consumable.this, region, origin, direction, quantity, r);
        }
    }

    private static final TextureRegion getRegion(String asset) {
        return GameScreen.getAtlasRegion("collectibles/" + asset);
    }
}
