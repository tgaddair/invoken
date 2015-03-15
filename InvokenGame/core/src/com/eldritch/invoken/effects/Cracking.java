package com.eldritch.invoken.effects;

import com.eldritch.invoken.activators.Activator;
import com.eldritch.invoken.activators.Crackable;
import com.eldritch.invoken.actor.aug.Crack;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.util.ActivationHandler;
import com.eldritch.invoken.actor.util.EnergyDrain.TimedEnergyDrain;

public class Cracking extends ActivatedEffect<Crack> {
    private static final float MAX_STRENGTH = 9; // cannot crack greater than this
    private static final float DURATION = 5; // no longer than this time to crack
    
    private final ActivationHandler handler = new CrackableHandler();
    private boolean active = false;

    public Cracking(Agent target, Crack aug, int cost) {
        super(target, aug, Crack.class, cost);
    }

    @Override
    protected void afterApply() {
        target.addActivationHandler(handler);
    }

    @Override
    protected void afterDispel() {
        target.removeActivationHandler(handler);
        target.resetCamera();
    }

    private class CrackableHandler implements ActivationHandler {
        @Override
        public boolean handle(Activator activator) {
            if (activator instanceof Crackable) {
                if (active) {
                    // only one activator allowed at a time
                    return true;
                }
                
                // only handle crackable activators
                return handle((Crackable) activator);
            }
            return false;
        }

        private boolean handle(Crackable activator) {
            // every 10 points of subterfuge increases our max lock strength
            int crackStrength = target.getInfo().getSubterfuge() / 10;
            if (activator.getStrength() > crackStrength || activator.getStrength() > MAX_STRENGTH) {
                return false;
            }

            // cost is a function of the lock strength
            float cost = getBaseCost() * activator.getStrength();

            // duration is a function of our inverse deception
            float duration = DURATION * (1.0f - target.getInfo().getDeception());

            addDrain(new CrackingDrain(activator, cost, duration));
            return true;
        }
    }

    private class CrackingDrain extends TimedEnergyDrain {
        private final Crackable activator;

        public CrackingDrain(Crackable activator, float cost, float duration) {
            super(target, cost, duration);
            this.activator = activator;
        }

        @Override
        protected void onElapsed() {
            activator.crack(target);
            active = false;
        }

        @Override
        protected void onDrained() {
            cancel();
        }
    }
}
