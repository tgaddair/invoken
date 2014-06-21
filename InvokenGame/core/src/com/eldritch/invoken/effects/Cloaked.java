package com.eldritch.invoken.effects;

import com.eldritch.invoken.actor.aug.Augmentation;
import com.eldritch.invoken.actor.type.Agent;

public class Cloaked extends BasicEffect {
    private final Augmentation aug;
    private final int cost;
    private float elapsed = 0;
    private boolean finished = false;
	
	public Cloaked(Agent target, Augmentation aug, int cost) {
	    super(target);
	    this.aug = aug;
	    this.cost = cost;
	}

	@Override
	public boolean isFinished() {
		return finished || (isApplied() && !target.isCloaked());
	}

	@Override
	public void dispel() {
	    target.setCloaked(false);
	    target.getInfo().getAugmentations().removeActiveAugmentation(aug);
	}
	
	@Override
    protected void doApply() {
	    target.setCloaked(true);
    }

    @Override
    protected void update(float delta) {
        if (target.getVelocity().isZero()) {
            // don't bother draining energy while not moving
            return;
        }
        
        elapsed += delta;
        if (elapsed > 1) {
            // drains continuously while moving
            if (cost <= target.getInfo().getEnergy()) {
                target.getInfo().expend(cost);
            } else {
                finished = true;
            }
            elapsed = 0;
        }
    }
}
