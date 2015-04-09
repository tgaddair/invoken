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
        System.out.println("before apply: " + target.getInfo().getMaxEnergy());
        target.getInfo().changeMaxEnergy(cost);
        System.out.println("after apply: " + target.getInfo().getMaxEnergy());
    }

    @Override
    public void dispel() {
        System.out.println("before dispel: " + target.getInfo().getMaxEnergy());
        target.getInfo().changeMaxEnergy(-cost);
        System.out.println("after dispel: " + target.getInfo().getMaxEnergy());
    }

    @Override
    public boolean isFinished() {
        return endCondition.satisfied();
    }

    @Override
    protected void update(float delta) {
    }
}
