package com.eldritch.invoken.actor.items;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.AgentInventory;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.actor.type.Agent.Direction;
import com.eldritch.invoken.actor.type.Collectible;
import com.eldritch.invoken.actor.type.Collectible.CollectibleGenerator;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.proto.Items;
import com.eldritch.invoken.proto.Items.Item.RangedWeaponType;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.state.Inventory;

public class Ammunition extends Item {
    private static final float AMMO_SCALE = 0.4f;
    
    private final AmmunitionGenerator generator = new AmmunitionGenerator();

    public Ammunition(Items.Item data) {
        super(data, 0);
    }

    public RangedWeaponType getType() {
        return data.getRangedType();
    }

    @Override
    public boolean isEquipped(AgentInventory inventory) {
        RangedWeaponType type = getType();
        return inventory.hasAmmunition(type) && inventory.getAmmunition(type) == this;
    }

    @Override
    public void addFrom(AgentInventory inventory) {
        if (!inventory.hasAmmunition(getType())) {
            equipFrom(inventory);
        }
    }

    @Override
    public void equipFrom(AgentInventory inventory) {
        inventory.setAmmunition(getType(), this);
    }

    @Override
    public void unequipFrom(AgentInventory inventory) {
        inventory.removeAmmunition(getType());
    }

    @Override
    public void releaseFrom(Inventory inventory, Level level, Vector2 position) {
        int total = inventory.getItemCount((this));
        generator.release(level, position, total);
        inventory.removeItem(this, total);
    }

    @Override
    protected Animation getAnimation(Activity activity, Direction direction) {
        // not animated
        return null;
    }

    @Override
    public String getTypeName() {
        return "Ammunition";
    }

    private static class AmmunitionEntity extends Collectible {
        private static final TextureRegion texture = GameScreen
                .getAtlasRegion("collectibles/ammo1");

        public AmmunitionEntity(Ammunition item, Vector2 origin, Vector2 direction, int quantity, float r) {
            super(item, quantity, texture, origin, direction, r);
        }

        @Override
        protected void onCollect(Agent agent) {
            // TODO: popup for collectibles
        }
    }

    private class AmmunitionGenerator extends CollectibleGenerator<AmmunitionEntity> {
        public AmmunitionGenerator() {
            super(AMMO_SCALE);
        }
        
        @Override
        protected AmmunitionEntity generate(Vector2 origin, Vector2 direction, int quantity, float r) {
            return new AmmunitionEntity(Ammunition.this, origin, direction, quantity, r);
        }
    }
}
