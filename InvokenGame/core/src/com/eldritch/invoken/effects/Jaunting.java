package com.eldritch.invoken.effects;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;

public class Jaunting extends BasicEffect {
	private final Vector2 target;
	private boolean finished = false;
	
    public Jaunting(Agent agent, Vector2 target) {
        super(agent);
        this.target = target;
    }

	@Override
	public boolean isFinished() {
		return finished;
	}

	@Override
	public void dispel() {
	}

	@Override
	protected void doApply() {
		getTarget().moveTo(target);
	}

	@Override
	protected void update(float delta) {
		if (getStateTime() > 1) {
			finished = true;
		} else if (getTarget().getPosition().dst2(target) < 1) {
			getTarget().stop();
			finished = true;
		}
	}
}
