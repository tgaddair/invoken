package com.eldritch.invoken.actor.items;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.AgentInfo;
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
    public void releaseFrom(AgentInventory inventory) {
        AgentInfo info = inventory.getAgentInfo();
        Agent agent = info.getAgent();
        Level level = agent.getLocation();
        
        int total = info.getInventory().getItemCount((Fragment.getInstance()));
        FragmentGenerator generator = FragmentGenerator.Holder.INSTANCE;
        generator.release(level, agent.getPosition(), total);
        info.getInventory().removeItem(Fragment.getInstance(), total);
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

        public FragmentEntity(Vector2 position, int quantity, float r) {
            super(Fragment.getInstance(), quantity, texture, position, r);
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
        protected FragmentEntity generate(Vector2 position, int quantity, float r) {
            return new FragmentEntity(position, quantity, r);
        }
        
        private static class Holder {
            private static final FragmentGenerator INSTANCE = new FragmentGenerator();
        }
    }
}
