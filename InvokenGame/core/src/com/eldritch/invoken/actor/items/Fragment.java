package com.eldritch.invoken.actor.items;

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
import com.eldritch.invoken.effects.SummonEnergy;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.proto.Items;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.state.Inventory;

public class Fragment extends Item {
    private static Fragment instance = null;

    private Fragment(Items.Item data) {
        // singleton
        super(data, 0);
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
    public void releaseFrom(Inventory inventory, Level level, Vector2 position) {
        int total = inventory.getItemCount((Fragment.getInstance()));
        FragmentGenerator generator = FragmentGenerator.Holder.INSTANCE;
        generator.release(level, position, total);
        inventory.removeItem(Fragment.getInstance(), total);
    }

    @Override
    protected Animation getAnimation(Activity activity, Direction direction) {
        // not animated
        return null;
    }

    @Override
    public String getTypeName() {
        return "Fragment";
    }

    public static Fragment getInstance() {
        if (instance != null) {
            return instance;
        }
        return getInstance(InvokenGame.ITEM_READER.readAsset("Fragment"));
    }

    public static Fragment getInstance(Items.Item data) {
        if (instance == null) {
            instance = new Fragment(data);
        }
        return instance;
    }

    private static class FragmentEntity extends Collectible {
        private static final TextureRegion texture = new TextureRegion(
                GameScreen.getTexture("sprite/effects/orb.png"));

        public FragmentEntity(Vector2 origin, Vector2 direction, int quantity, float r) {
            super(Fragment.getInstance(), quantity, texture, origin, direction, r);
        }

        @Override
        protected void onCollect(Agent agent) {
            // check that we can level up now
            if (agent.getInfo().canLevel() && !agent.getInfo().couldLevel(-getQuantity())) {
                agent.getLocation().addEntity(SummonEnergy.getEntity(agent, 2, 0.15f));
            }
        }
    }

    private static class FragmentGenerator extends CollectibleGenerator<FragmentEntity> {
        @Override
        protected FragmentEntity generate(Vector2 origin, Vector2 direction, int quantity, float r) {
            return new FragmentEntity(origin, direction, quantity, r);
        }
        
        private static class Holder {
            private static final FragmentGenerator INSTANCE = new FragmentGenerator();
        }
    }
}
