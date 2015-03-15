package com.eldritch.invoken.actor.util;

import com.eldritch.invoken.actor.type.Agent;

public abstract class EnergyDrain {
    private final Agent target;
    private final float cost;
    private boolean finished = false;
    
    public EnergyDrain(Agent target, float cost) {
        this.target = target;
        this.cost = cost;
    }
    
    public void update(float delta) {
        // drains continuously while active
        float c = cost * delta;
        if (c <= target.getInfo().getEnergy()) {
            target.getInfo().expend(c);
        } else {
            finished = true;
            onDrained();
        }
    }
    
    protected final void finish() {
        finished = true;
    }
    
    public boolean isFinished() {
        return finished;
    }
    
    protected abstract void onDrained();
    
    public abstract static class TimedEnergyDrain extends EnergyDrain {
        private final float duration;
        private float elapsed = 0;
        
        public TimedEnergyDrain(Agent target, float cost, float duration) {
            super(target, cost);
            this.duration = duration;
        }
        
        public void update(float delta) {
            super.update(delta);
            
            if (!isFinished()) {
                elapsed += delta;
                if (elapsed >= duration) {
                    // trigger end of timer
                    finish();
                    onElapsed();
                }
            }
        }
        
        protected abstract void onElapsed();
    }
}
