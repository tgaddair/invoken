package com.eldritch.invoken.actor.type;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.items.Item;
import com.eldritch.invoken.location.Level;

public abstract class Collectible extends CollisionEntity implements TemporaryEntity {
    private static final float MOVE_RADIUS = 25f;
    private static final float OBTAIN_RADIUS = 0.1f;

    private final Item item;
    private final TextureRegion texture;
    private final int quantity;
    private boolean finished = false;

    public Collectible(Item item, int quanitity, TextureRegion texture, Vector2 position, float r) {
        super(position, r, r);
        this.item = item;
        this.quantity = quanitity;
        this.texture = texture;
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
        agent.getInventory().addItem(item, quantity);
        onCollect(agent);
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
            Vector2 position = origin.cpy().add((float) Math.random(), (float) Math.random());
            float r = (float) Math.log(quantity) / 5f;
            return generate(position, quantity, r);
        }

        protected abstract T generate(Vector2 position, int quantity, float size);
    }
}
