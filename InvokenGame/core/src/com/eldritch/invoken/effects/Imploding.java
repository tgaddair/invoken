package com.eldritch.invoken.effects;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;

public class Imploding extends BasicEffect {
	private final Vector2 target;
	private final float duration;
	private final float magnitude;
	
    public Imploding(Agent agent, Agent owner, Vector2 target, float duration, float magnitude) {
        super(agent);
        this.target = target;
        this.duration = duration;
        this.magnitude = magnitude * owner.getAttackScale(agent);
    }

	@Override
	public boolean isFinished() {
		return getStateTime() > duration;
	}

	@Override
	public void dispel() {
	}

	@Override
	protected void doApply() {
	}

	@Override
	protected void update(float delta) {
		if (getTarget().getPosition().dst2(target) > 0.001) {
			Vector2 direction = target.cpy().sub(getTarget().getPosition()).nor();
			getTarget().applyForce(direction.scl(delta * magnitude));
		}
	}
}
