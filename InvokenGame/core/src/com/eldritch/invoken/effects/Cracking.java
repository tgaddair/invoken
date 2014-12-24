package com.eldritch.invoken.effects;

import com.eldritch.invoken.actor.aug.Augmentation;
import com.eldritch.invoken.actor.aug.Crack;
import com.eldritch.invoken.actor.type.Agent;

public class Cracking extends BasicEffect {
    private final Augmentation aug;
    private final int cost;
    private float elapsed = 0;
    private boolean finished = false;
	
	public Cracking(Agent target, Augmentation aug, int cost) {
	    super(target);
	    this.aug = aug;
	    this.cost = cost;
	}

	@Override
	public boolean isFinished() {
		return finished || (isApplied() && !target.isToggled(Crack.class));
	}

	@Override
	public void dispel() {
	    target.toggleOff(Crack.class);
	    target.getInfo().getAugmentations().removeActiveAugmentation(aug);
	    target.resetCamera();
	}
	
	@Override
    protected void doApply() {
	    target.toggleOn(Crack.class);
    }

    @Override
    protected void update(float delta) {
        elapsed += delta;
        if (elapsed > 1) {
            // drains continuously while active
            if (cost <= target.getInfo().getEnergy()) {
                target.getInfo().expend(cost);
            } else {
                finished = true;
            }
            elapsed = 0;
        }
    }
}
