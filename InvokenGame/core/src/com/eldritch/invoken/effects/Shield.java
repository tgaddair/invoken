package com.eldritch.invoken.effects;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.eldritch.invoken.actor.aug.Augmentation;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Projectile;
import com.eldritch.invoken.actor.type.Projectile.ProjectileHandler;
import com.eldritch.invoken.screens.GameScreen;

public class Shield extends AnimatedEffect {
    private final Augmentation aug;
    private final ProjectileHandler handler = new ShieldProjectileHandler();
    private final int magnitude = 100;
    
	public Shield(Agent actor, Augmentation aug) {
		super(actor, GameScreen.getRegions("sprite/effects/shield.png", 96, 96)[2],
				Animation.PlayMode.LOOP);
		this.aug = aug;
	}
	
    @Override
    public void doApply() {
        target.getInfo().modActiveDefense(magnitude);
        target.addProjectileHandler(handler);
    }
    
    @Override
    public void dispel() {
        target.getInfo().modActiveDefense(-magnitude);
        target.removeProjectileHandler(handler);
        target.getInfo().getAugmentations().removeActiveAugmentation(aug);
    }
    
	@Override
	public boolean isFinished() {
		return !getTarget().isToggled(Shield.class);
	}
	
	private class ShieldProjectileHandler implements ProjectileHandler {
        @Override
        public boolean handle(Projectile projectile) {
            target.getInfo().expend(magnitude / 20f);
            if (target.getInfo().getEnergy() < magnitude / 20f) {
                target.toggleOff(Shield.class);
            }
            return false;
        }
	}
}
