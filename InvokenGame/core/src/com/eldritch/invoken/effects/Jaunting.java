package com.eldritch.invoken.effects;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;

public class Jaunting extends BasicEffect {
	private final Vector2 target;
	private boolean arrived = false;
	
    public Jaunting(Agent agent, Vector2 target) {
        super(agent);
        this.target = target;
    }

	@Override
	public boolean isFinished() {
		return arrived || getStateTime() > 1;
	}

	@Override
	public void dispel() {
	}

	@Override
	protected void doApply() {
		Vector2 direction = target.cpy().sub(getTarget().getPosition()).nor();
    	getTarget().applyForce(direction.scl(2500));
	}

	@Override
	protected void update(float delta) {
		if (getTarget().getPosition().dst2(target) < 1) {
			getTarget().stop();
			arrived = true;
		}
	}
}
