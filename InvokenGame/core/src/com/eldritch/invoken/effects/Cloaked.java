package com.eldritch.invoken.effects;

import com.eldritch.invoken.actor.aug.Augmentation;
import com.eldritch.invoken.actor.type.Agent;

public class Cloaked extends BasicEffect {
    private final Augmentation aug;
    private final int cost;
    private boolean finished = false;

    public Cloaked(Agent target, Augmentation aug, int cost) {
        super(target);
        this.aug = aug;
        this.cost = cost;
    }

    @Override
    protected void doApply() {
        target.setCloaked(true);
        target.setStunted(true); // cannot regain energy
    }

    @Override
    public void dispel() {
        target.setCloaked(false);
        target.setStunted(false);
        target.getInfo().getAugmentations().removeSelfAugmentation(aug);
    }

    @Override
    public boolean isFinished() {
        return finished || (isApplied() && !target.isCloaked());
    }

    @Override
    protected void update(float delta) {
        if (target.getVelocity().isZero()) {
            // don't bother draining energy while not moving
            return;
        }

        // drains continuously while moving
        float c = cost * delta;
        if (c <= target.getInfo().getEnergy()) {
            target.getInfo().expend(c);
        } else {
            finished = true;
        }
    }
}
