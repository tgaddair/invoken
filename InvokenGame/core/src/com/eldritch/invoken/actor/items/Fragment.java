package com.eldritch.invoken.actor.items;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.Inventory;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.actor.type.Agent.Direction;
import com.eldritch.invoken.actor.type.CollisionEntity;
import com.eldritch.invoken.actor.type.TemporaryEntity;
import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.proto.Items;

public class Fragment extends Item {
    private static Fragment instance = null;
    
    private Fragment(Items.Item data) {
        // singleton
        super(data, 0);
    }

    @Override
    public boolean isEquipped(Inventory inventory) {
        // cannot be equipped
        return false;
    }

    @Override
    public void equipFrom(Inventory inventory) {
    }

    @Override
    public void unequipFrom(Inventory inventory) {
    }

    @Override
    protected Animation getAnimation(Activity activity, Direction direction) {
        // not animated
        return null;
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
    
    public static void release(Location location, Vector2 origin, int count) {
        int remaining = count;
        while (remaining > 0) {
            int quantity = (int) (Math.random() * remaining) + 1;  // [1, remaining]
            FragmentEntity entity = FragmentEntity.of(origin, quantity);
            location.addEntity(entity);
        }
    }
    
    private static class FragmentEntity extends CollisionEntity implements TemporaryEntity {
        private final int quantity;
        private boolean finished = false;
        
        public FragmentEntity(Vector2 position, int quanitity, float r) {
            super(r, r);
            this.position.set(position);
            this.quantity = quanitity;
        }
        
        @Override
        public void update(float delta, Location location) {
        }

        @Override
        public void render(float delta, OrthogonalTiledMapRenderer renderer) {
        }

        @Override
        public boolean isFinished() {
            return finished;
        }

        @Override
        public void dispose() {
        }
        
        public static FragmentEntity of(Vector2 origin, int quantity) {
            // shift the origin by a random amount
            Vector2 position = origin.cpy().add((float) Math.random(), (float) Math.random());
            float r = quantity / 100f;
            return new FragmentEntity(position, quantity, r);
        }
    }
}
