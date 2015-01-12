package com.eldritch.invoken.effects;

import com.eldritch.invoken.actor.type.Agent;

public class Resurrected extends BasicEffect {
	private final Agent owner;
	private final float cost;
	private boolean applied = false;
	
	public Resurrected(Agent owner, Agent target, float cost) {
	    super(target);
	    this.owner = owner;
		this.cost = cost;
	}

	@Override
	public boolean isFinished() {
		return applied && !target.isAlive();
	}

	@Override
	public void dispel() {
		owner.getInfo().changeBaseEnergy(cost);
	}
	
	@Override
    protected void doApply() {
		owner.addFollower(target);
		target.resurrect();
		owner.getInfo().changeBaseEnergy(-cost);
		applied = true;
    }

    @Override
    protected void update(float delta) {
    }
}
