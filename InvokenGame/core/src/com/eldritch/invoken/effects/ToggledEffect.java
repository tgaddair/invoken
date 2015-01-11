package com.eldritch.invoken.effects;

import com.eldritch.invoken.actor.aug.Augmentation;
import com.eldritch.invoken.actor.type.Agent;

public abstract class ToggledEffect<T extends Augmentation> extends BasicEffect {
    private final T aug;
    private final Class<T> toggle;
    private final int cost;
    private float elapsed = 0;
    private boolean finished = false;
	
	public ToggledEffect(Agent target, T aug, Class<T> toggle, int cost) {
	    super(target);
	    this.aug = aug;
	    this.toggle = toggle;
	    this.cost = cost;
	}

	@Override
	public boolean isFinished() {
		return finished || (isApplied() && !target.isToggled(toggle));
	}

	@Override
	public void dispel() {
	    target.toggleOff(toggle);
	    target.getInfo().getAugmentations().removeSelfAugmentation(aug);
	    afterDispel();
	}
	
	@Override
    protected void doApply() {
	    target.toggleOn(toggle);
	    afterApply();
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
    
    protected abstract void afterApply();
    
    protected abstract void afterDispel();
}
