package com.eldritch.invoken.effects;

import com.eldritch.invoken.actor.type.Agent;

public class HoldingWeapon extends BasicEffect {
    public HoldingWeapon(Agent agent) {
        super(agent);
    }

    @Override
    public void doApply() {
        target.getLocation().addEntity(target.getWeaponSentry());
    }

    @Override
    public void dispel() {
    }

    @Override
    public boolean isFinished() {
        return target.getWeaponSentry().isFinished();
    }

    @Override
    protected void update(float delta) {
    }
}
