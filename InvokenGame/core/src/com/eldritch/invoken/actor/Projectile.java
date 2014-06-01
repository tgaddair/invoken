package com.eldritch.invoken.actor;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.eldritch.invoken.encounter.Location;

public abstract class Projectile extends CollisionEntity implements TemporaryEntity {
    private final float speed;
    private Agent owner;
    private boolean finished;
    private float stateTime;

    public Projectile(float width, float height, float speed) {
        super(width, height);
        this.speed = speed;
    }

    public void setup(Agent source, Agent target) {
        owner = source;
        finished = false;
        stateTime = 0;

        position.set(source.getForwardVector().scl(0.5f).add(source.getPosition()));
        velocity.set(target.getPosition());
        velocity.sub(source.getPosition());
        velocity.nor();
    }

    @Override
    public void update(float delta, Location location) {
        stateTime += delta;

        float scale = speed * delta;
        velocity.scl(scale);
        position.add(velocity);
        velocity.scl(1 / scale);

        float x = position.x + velocity.x * 0.25f;
        float y = position.y + velocity.y * 0.25f;
        for (Agent agent : getCollisionActors(location)) {
            if (agent != owner && agent.collidesWith(x, y)) {
                apply(agent);
                return;
            }
        }

        location.getTiles((int) (x - 1), (int) (y - 1), (int) (x + 1), (int) (y + 1),
                location.getTiles());
        for (Rectangle tile : location.getTiles()) {
            if (tile.contains(x, y)) {
                finish();
                return;
            }
        }
        
        // finally, reclaim the projectile if it has been active for more than 10 seconds
        if (stateTime >= 10) {
            finish();
        }
    }

    @Override
    public void render(float delta, OrthogonalTiledMapRenderer renderer) {
        Batch batch = renderer.getSpriteBatch();
        batch.begin();
        preRender(batch);
        batch.draw(getTexture(stateTime), position.x - getWidth() * 0.5f, position.y - getHeight()
                * 0.5f, 0.5f, 0.5f, getWidth(), getHeight(), 1f, 1f, velocity.angle());
        postRender(batch);
        batch.end();
    }
    
    protected void preRender(Batch batch) {
    }
    
    protected void postRender(Batch batch) {
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    private void apply(Agent target) {
        apply(owner, target);
        finish();
    }

    private void finish() {
        finished = true;
        free();
    }

    protected abstract void apply(Agent owner, Agent target);

    protected abstract TextureRegion getTexture(float delta);

    protected abstract void free();
}