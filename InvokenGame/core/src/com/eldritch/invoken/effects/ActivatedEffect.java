package com.eldritch.invoken.effects;

import java.util.Iterator;
import java.util.List;

import com.eldritch.invoken.actor.aug.Augmentation;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.util.EnergyDrain;
import com.google.common.collect.Lists;

public abstract class ActivatedEffect<T extends Augmentation> extends BasicEffect {
    private final T aug;
    private final Class<T> toggle;
    private final int cost;
    private final List<EnergyDrain> drains = Lists.newArrayList();
    private boolean finished = false;

    public ActivatedEffect(Agent target, T aug, Class<T> toggle, int cost) {
        super(target);
        this.aug = aug;
        this.toggle = toggle;
        this.cost = cost;
    }

    @Override
    protected void doApply() {
        target.toggleOn(toggle);
        target.setStunted(true); // cannot regain energy
        afterApply();
    }

    @Override
    public void dispel() {
        target.toggleOff(toggle);
        target.setStunted(false);
        target.getInfo().getAugmentations().removeSelfAugmentation(aug);
        afterDispel();
    }

    @Override
    public boolean isFinished() {
        return finished || (isApplied() && !target.isToggled(toggle));
    }

    @Override
    protected void update(float delta) {
        // drains continuously while active
        for (EnergyDrain drain : drains) {
            drain.update(delta);
        }

        // remove finished drains
        Iterator<EnergyDrain> it = drains.iterator();
        while (it.hasNext()) {
            EnergyDrain drain = it.next();
            if (drain.isFinished()) {
                it.remove();
            }
        }
    }

    protected final void addDrain(EnergyDrain drain) {
        drains.add(drain);
    }

    protected final void cancel() {
        finished = true;
    }

    public int getBaseCost() {
        return cost;
    }

    protected abstract void afterApply();

    protected abstract void afterDispel();
}
