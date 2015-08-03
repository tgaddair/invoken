package com.eldritch.invoken.actor.type;

import java.util.Random;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.items.Item;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.util.Settings;
import com.eldritch.invoken.util.SoundManager.SoundEffect;

public abstract class Collectible extends CollisionEntity implements TemporaryEntity {
    private static final float MOVE_RADIUS = 2f * 2f;
    private static final float OBTAIN_RADIUS = 0.1f;

    private final Item item;
    private final TextureRegion texture;
    private final int quantity;
    private boolean finished = false;

    public Collectible(Item item, int quanitity, TextureRegion texture, Vector2 origin,
            Vector2 direction, float r) {
        super(origin, r, r);
        this.item = item;
        this.quantity = quanitity;
        this.texture = texture;

        velocity.set(direction).scl(0.1f);
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
            // apply friction
            velocity.scl(0.95f * (1f - delta));
            if (velocity.epsilonEquals(Vector2.Zero, Settings.EPSILON)) {
                velocity.set(Vector2.Zero);
            }
        }
    }

    private void grantTo(Agent agent) {
        finished = true;
        agent.getInventory().addItem(item, quantity);
        onCollect(agent);
        InvokenGame.SOUND_MANAGER.playAtPoint(SoundEffect.COLLECT, agent.getPosition());
    }

    protected abstract void onCollect(Agent agent);

    @Override
    public void render(float delta, OrthogonalTiledMapRenderer renderer) {
        float width = getWidth();
        float height = getHeight();
        Batch batch = renderer.getBatch();
        batch.begin();
        batch.draw(texture, position.x - width / 2, position.y - height / 2, width, height);
        batch.end();
    }

    public int getQuantity() {
        return quantity;
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public void dispose() {
    }

    /**
     * Adds collectible temporary entities to the world in a radial pattern around the agent for
     * players, we must also assign the collectibles to the nearest unique neighbor (if possible)
     * for persistence.
     */
    public abstract static class CollectibleGenerator<T extends Collectible> {
        private static final float MIN_SIZE = 0.5f;
        
        private final Random rand = new Random();
        private final float scale;

        public CollectibleGenerator() {
            this(1 / 5f);
        }

        public CollectibleGenerator(float scale) {
            this.scale = scale;
        }

        public void release(Level level, Vector2 origin, int count) {
            int remaining = count;
            while (remaining > 0) {
                int quantity = (int) (Math.random() * remaining) + 1; // [1, remaining]
                T entity = generateShifted(origin, quantity);
                level.addEntity(entity);
                remaining -= quantity;
            }
        }

        private T generateShifted(Vector2 origin, int quantity) {
            // shift the origin by a random amount
            Vector2 direction = new Vector2(random(), random());
            float r = getSize(quantity);
            return generate(origin, direction, quantity, r);
        }
        
        protected float getSize(int quantity) {
            return Math.max((float) Math.log(quantity) * scale, MIN_SIZE);
        }

        private float random() {
            // [-1, 1]
            return (rand.nextFloat() * 2f) - 1f;
        }

        protected abstract T generate(Vector2 origin, Vector2 direction, int quantity, float size);
    }
}
