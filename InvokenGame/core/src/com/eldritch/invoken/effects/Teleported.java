package com.eldritch.invoken.effects;

import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.location.NaturalVector2;

public class Teleported extends BasicEffect {
    private final NaturalVector2 destination;
	private boolean applied = false;
	
	public Teleported(Agent owner, NaturalVector2 destination) {
	    super(owner);
	    this.destination = destination;
	}

	@Override
	public boolean isFinished() {
		return applied;
	}

	@Override
	public void dispel() {
	}
	
	@Override
    protected void doApply() {
		target.teleport(destination.toVector2());
		applied = true;
    }

    @Override
    protected void update(float delta) {
    }
}
