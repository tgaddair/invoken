package com.eldritch.invoken.effects;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.eldritch.invoken.actor.aug.Augmentation;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.HandledBullet;
import com.eldritch.invoken.actor.type.HandledProjectile;
import com.eldritch.invoken.actor.type.HandledProjectile.ProjectileHandler;
import com.eldritch.invoken.screens.GameScreen;

public class Mirroring extends AnimatedEffect {
    private final Augmentation aug;
    private final ProjectileHandler handler = new MirrorProjectileHandler();
    
	public Mirroring(Agent target, Augmentation aug) {
		super(target, GameScreen.getRegions("sprite/effects/shield.png", 96, 96)[2],
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
		return !getTarget().isToggled(Mirroring.class);
	}
	
	private class MirrorProjectileHandler implements ProjectileHandler {
        @Override
        public boolean handle(HandledProjectile projectile) {
            float damage = projectile.getDamage(target);
            if (damage > 0) {
                target.getInfo().expend(damage);
                if (target.getInfo().getEnergy() < damage) {
                    target.toggleOff(Mirroring.class);
                }
                
                projectile.reset(target, projectile.getOwner().getPosition());
                return true;
            }
            return false;
        }
	}
}
