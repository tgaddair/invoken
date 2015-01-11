package com.eldritch.invoken.effects;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.eldritch.invoken.actor.aug.Augmentation;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.HandledProjectile;
import com.eldritch.invoken.actor.type.HandledProjectile.ProjectileHandler;
import com.eldritch.invoken.screens.GameScreen;

public class Shield extends AnimatedEffect {
    private final Augmentation aug;
    private final ProjectileHandler handler = new ShieldProjectileHandler();
    
	public Shield(Agent actor, Augmentation aug) {
		super(actor, GameScreen.getRegions("sprite/effects/shield.png", 96, 96)[2],
				Animation.PlayMode.LOOP);
		this.aug = aug;
	}
	
    @Override
    public void doApply() {
        target.addProjectileHandler(handler);
    }
    
    @Override
    public void dispel() {
        target.removeProjectileHandler(handler);
        target.getInfo().getAugmentations().removeSelfAugmentation(aug);
    }
    
	@Override
	public boolean isFinished() {
		return !getTarget().isToggled(Shield.class);
	}
	
	private class ShieldProjectileHandler implements ProjectileHandler {
        @Override
        public boolean handle(HandledProjectile handledProjectile) {
            float damage = handledProjectile.getDamage(target);
            if (damage > 0) {
                target.getInfo().expend(damage);
                if (target.getInfo().getEnergy() < damage) {
                    target.toggleOff(Shield.class);
                }
                
                handledProjectile.cancel();
                return true;
            }
            return false;
        }
	}
}
