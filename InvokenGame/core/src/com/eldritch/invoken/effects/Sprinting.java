package com.eldritch.invoken.effects;

import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.util.EnergyDrain;

public class Sprinting extends BasicEffect {
    private final float scale;
    private final EnergyDrain drain;

    public Sprinting(Agent target, float scale, float cost) {
        super(target);
        this.scale = scale;
        this.drain = new SprintingDrain(target, cost);
    }

    @Override
    protected void doApply() {
        target.scaleLinearVelocity(scale);
        target.setStunted(true); // cannot regain energy
    }
    
    @Override
    public void dispel() {
        target.scaleLinearVelocity(-scale);
        target.setStunted(false);
    }
    
    @Override
    public boolean isFinished() {
        return !target.isSprinting() || drain.isFinished();
    }

    @Override
    protected void update(float delta) {
        drain.update(delta);
    }
    
    private class SprintingDrain extends EnergyDrain {
        public SprintingDrain(Agent target, float cost) {
            super(target, cost);
        }

        @Override
        protected void onDrained() {
            // do nothing
        }
    }
}
