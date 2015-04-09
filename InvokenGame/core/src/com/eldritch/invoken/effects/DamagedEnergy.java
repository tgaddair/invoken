package com.eldritch.invoken.effects;

import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.util.Condition;

public class DamagedEnergy extends BasicEffect {
    private final float cost;
    private final Condition endCondition;
    
    public DamagedEnergy(Agent agent, float cost, Condition endCondition) {
        super(agent);
        this.cost = cost;
        this.endCondition = endCondition;
    }

    @Override
    public void doApply() {
        target.getInfo().changeMaxEnergy(-cost);
    }

    @Override
    public void dispel() {
        target.getInfo().changeMaxEnergy(cost);
    }

    @Override
    public boolean isFinished() {
        return endCondition.satisfied();
    }

    @Override
    protected void update(float delta) {
    }
}
