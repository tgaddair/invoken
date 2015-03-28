package com.eldritch.invoken.effects;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.aug.Augmentation;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.HandledProjectile;
import com.eldritch.invoken.actor.type.HandledProjectile.ProjectileHandler;
import com.eldritch.invoken.screens.GameScreen;

public class Shield extends BasicEffect {
    private static final float V_PENALTY = 5;
    
    private final Augmentation aug;
    private static final TextureRegion region = new TextureRegion(
            GameScreen.getTexture("sprite/effects/shield1.png"));
    private final ProjectileHandler handler = new ShieldProjectileHandler();
    
	public Shield(Agent actor, Augmentation aug) {
		super(actor);
		this.aug = aug;
	}
	
    @Override
    public void doApply() {
        target.addProjectileHandler(handler);
        target.setStunted(true);  // cannot regain energy when shielded
        target.addVelocityPenalty(V_PENALTY);  // shielding slows down the caster
    }
    
    @Override
    public void dispel() {
        target.removeProjectileHandler(handler);
        target.getInfo().getAugmentations().removeSelfAugmentation(aug);
        target.setStunted(false);
        target.addVelocityPenalty(-V_PENALTY);
    }
    
	@Override
	public boolean isFinished() {
		return !getTarget().isToggled(Shield.class);
	}
	
    @Override
    protected void update(float delta) {
    }
    
    @Override
    public void render(float delta, OrthogonalTiledMapRenderer renderer) {
        Vector2 position = target.getRenderPosition();
        Vector2 direction = target.getWeaponSentry().getDirection();
        float width = target.getWidth();
        float height = target.getHeight();
        
        Batch batch = renderer.getBatch();
        batch.begin();
        batch.draw(region,
                position.x - width / 2, position.y - height / 2,  // position
                width / 2, height / 2,  // origin
                width, height,  // size
                1f, 1f,  // scale
                direction.angle());
        batch.end();  
    }
	
	private class ShieldProjectileHandler implements ProjectileHandler {
        @Override
        public boolean handle(HandledProjectile projectile) {
            float damage = projectile.getDamage(target);
            if (damage > 0) {
                target.getInfo().expend(damage);
                if (target.getInfo().getEnergy() < damage) {
                    target.toggleOff(Shield.class);
                }
                
                projectile.cancel();
                return true;
            }
            return false;
        }
	}
}
