package com.eldritch.invoken.actor.type;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.encounter.Location;

public abstract class AoeProjectile extends Projectile {
    private final TextureRegion texture;
    private final float radius;
    private final Animation explosion;
    
    private Vector2 target = null;
    private float explosionTime = 0;
    private boolean detonated = false;

    public AoeProjectile(TextureRegion texture, TextureRegion[] explosionRegions,
    		float speed, float damage, float radius) {
        super(texture, speed, damage);
        this.texture = texture;
        this.radius = radius;
        explosion = new Animation(0.1f, explosionRegions);
    }
    
    public float getRadius() {
    	return radius;
    }
    
    @Override
    public void setup(Agent source, Vector2 target) {
    	super.setup(source, target);
    	this.target = target;
    	explosionTime = 0;
    	detonated = false;
    }
    
    @Override
    public boolean handleBeforeUpdate(float delta, Location location) {
    	if (detonated) {
    		// update the explosion
    		explosionTime += delta;
    		
    		// cancel the projectile if we're done with the explosion
    		if (explosion.isAnimationFinished(explosionTime)) {
    			cancel();
    		} else {
    			doDuringExplosion(delta, location);
    		}
    	} else if (position.dst2(target) < 0.5f) {
    		// special case where the position is very close to the target
    		detonate();
    	}
    	return detonated;
    }
    
    @Override
    public void render(float delta, OrthogonalTiledMapRenderer renderer) {
    	if (detonated) {
    		// render the explosion
    		float width = radius * 2;
    		float height = radius * 2;
    		Batch batch = renderer.getBatch();
            batch.begin();
            batch.draw(explosion.getKeyFrame(explosionTime),
            		position.x - width / 2, position.y - height / 2, width, height);
            batch.end();
    	} else {
    		// render the projectile itself
    		super.render(delta, renderer);
    	}
    }

    @Override
    protected TextureRegion getTexture(float stateTime) {
        return texture;
    }

	@Override
	protected void handleAgentContact(Agent agent) {
		detonate();
	}

	@Override
	protected void handleObstacleContact() {
		detonate();
	}
	
	private void detonate() {
		onDetonate();
		detonated = true;
	}
	
	protected abstract void onDetonate();
	
	protected abstract void doDuringExplosion(float delta, Location location);

}