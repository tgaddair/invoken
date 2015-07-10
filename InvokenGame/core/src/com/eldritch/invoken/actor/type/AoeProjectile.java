package com.eldritch.invoken.actor.type;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.util.Damage;

public abstract class AoeProjectile extends Projectile {
    private final TextureRegion texture;
    private final float radius;
    
    private final Vector2 target;
    private boolean detonated = false;

    public AoeProjectile(Agent owner, Vector2 target, TextureRegion texture, TextureRegion[] explosionRegions,
    		float speed, Damage damage, float radius) {
        super(owner, texture, speed, damage);
        this.target = target;
        this.texture = texture;
        this.radius = radius;
    }
    
    public float getRadius() {
    	return radius;
    }
    
    @Override
    public boolean handleBeforeUpdate(float delta, Level level) {
        Vector2 position = getPosition();
    	if (!detonated && position.dst2(target) < 0.5f) {
    		// special case where the position is very close to the target
    		detonate();
    	}
    	return detonated;
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
	    if (!detonated) {
	        onDetonate();
	        detonated = true;
	        cancel();
	    }
	}
	
	protected abstract void onDetonate();
	
	protected abstract void doDuringExplosion(float delta, Level level);

}