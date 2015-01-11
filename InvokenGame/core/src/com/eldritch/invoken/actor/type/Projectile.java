package com.eldritch.invoken.actor.type;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.encounter.Location;

public abstract class Projectile extends CollisionEntity implements TemporaryEntity {
	private final float speed;
    private final float damage;
    private Agent owner;
    private boolean finished;
    private float stateTime;

    public Projectile(float width, float height, float speed, float damage) {
        super(width, height);
        this.speed = speed;
        this.damage = damage;
    }
    
    public float getSpeed() {
        return speed;
    }
    
    public float getDamage(Agent target) {
        return damage * owner.getAttackScale(target);
    }
    
    public Agent getOwner() {
        return owner;
    }
    
    public void setup(Agent source, Agent target) {
        setup(source, target.getPosition());
    }
    
    public void setup(Agent source, Vector2 target) {
        finished = false;
        stateTime = 0;

        // get the center of the agent launching the projectile, then offset the starting position
        // slightly towards its target so it appears to be coming from the agent's hands
        Vector2 center = source.getVisibleCenter();
        Vector2 delta = target.cpy().sub(center).nor().scl(0.5f);
        position.set(center.add(delta));
        reset(source, target);
    }
    
    public void reset(Agent source, Vector2 target) {
        owner = source;
        
        velocity.set(target);
        velocity.sub(position);
        velocity.nor();
    }

    @Override
    public void update(float delta, Location location) {
        stateTime += delta;
        
        if (handleBeforeUpdate(delta, location)) {
        	return;
        }

        float scale = speed * delta;
        velocity.scl(scale);
        position.add(velocity);
        velocity.scl(1 / scale);

        float x = position.x + velocity.x * 0.25f;
        float y = position.y + velocity.y * 0.25f;
        for (Agent agent : getCollisionActors(location)) {
            if (agent != owner && agent.collidesWith(x, y)) {
            	handleAgentContact(agent);
                return;
            }
        }

        location.getTiles((int) (x - 1), (int) (y - 1), (int) (x + 1), (int) (y + 1),
                location.getTiles());
        for (Rectangle tile : location.getTiles()) {
            if (tile.contains(x, y)) {
            	handleObstacleContact();
                return;
            }
        }
        
        // finally, reclaim the projectile if it has been active for more than 10 seconds
        if (stateTime >= 10) {
            finish();
        }
    }
    
    public boolean handleBeforeUpdate(float delta, Location location) {
    	return false;
    }

    @Override
    public void render(float delta, OrthogonalTiledMapRenderer renderer) {
        Batch batch = renderer.getSpriteBatch();
        batch.begin();
        preRender(batch);
        batch.draw(getTexture(stateTime),
        		position.x - getWidth(), position.y - getHeight(),
        		getWidth() / 2, getHeight() / 2,
        		getWidth(), getHeight(), 1f, 1f, velocity.angle());
        postRender(batch);
        batch.end();
    }
    
    protected void preRender(Batch batch) {
    }
    
    protected void postRender(Batch batch) {
    }
    
    public void cancel() {
        finish();
    }

    private void finish() {
        finished = true;
        free();
    }
    
    @Override
    public boolean isFinished() {
        return finished;
    }

    protected abstract TextureRegion getTexture(float delta);

    protected abstract void free();
    
    protected abstract void handleAgentContact(Agent agent);
    
    protected abstract void handleObstacleContact();
}
