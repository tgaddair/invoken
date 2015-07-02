package com.eldritch.invoken.actor.items;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.AgentInventory;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.actor.type.Agent.Direction;
import com.eldritch.invoken.actor.type.CollisionEntity;
import com.eldritch.invoken.actor.type.TemporaryEntity;
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
    public void equipFrom(AgentInventory inventory) {
    }

    @Override
    public void unequipFrom(AgentInventory inventory) {
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

    public static void release(Level level, Vector2 origin, int count) {
        int remaining = count;
        while (remaining > 0) {
            int quantity = (int) (Math.random() * remaining) + 1; // [1, remaining]
            FragmentEntity entity = FragmentEntity.of(origin, quantity);
            level.addEntity(entity);
            remaining -= quantity;
        }
    }

    private static class FragmentEntity extends CollisionEntity implements TemporaryEntity {
        private static final float MOVE_RADIUS = 25f;
        private static final float OBTAIN_RADIUS = 0.1f;
        
        private static final TextureRegion texture = new TextureRegion(
                GameScreen.getTexture("sprite/effects/orb.png"));

        private final int quantity;
        private boolean finished = false;

        public FragmentEntity(Vector2 position, int quanitity, float r) {
            super(position, r, r);
            this.quantity = quanitity;
        }

        @Override
        public void update(float delta, Level level) {
            // update our position
            position.add(velocity);
            
            // find the closest entity
            Agent closest = null;
            float bestD = Float.POSITIVE_INFINITY;
            for (Agent agent : level.getActiveEntities()) {
                if (agent.getInfo().isUnique() && agent.isAlive()) {
                    // move towards closest
                    float d = agent.getPosition().dst2(getPosition());
                    if (d < MOVE_RADIUS && d < bestD) {
                        closest = agent;
                        bestD = d;
                    }
                }
            }
            
            // when we find a closest entity, we either move towards it or grant the fragments
            // when within the obtain radius
            if (closest != null) {
                if (bestD < OBTAIN_RADIUS) {
                    // grant the fragments
                    grantTo(closest);
                } else {
                    // move towards the closest
                    velocity.set(closest.getPosition());
                    velocity.sub(getPosition());
                    velocity.nor().scl(0.05f);
                }
            } else {
                // stop moving
                velocity.set(Vector2.Zero);
            }
        }
        
        private void grantTo(Agent agent) {
            finished = true;
            agent.getInventory().addItem(Fragment.getInstance(), quantity);
        }

        @Override
        public void render(float delta, OrthogonalTiledMapRenderer renderer) {
            float width = getWidth();
            float height = getHeight();
            Batch batch = renderer.getBatch();
            batch.begin();
            batch.draw(texture, position.x - width / 2, position.y - height / 2, width, height);
            batch.end();
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
            float r = (float) Math.log(quantity) / 5f;
            return new FragmentEntity(position, quantity, r);
        }
    }
}
