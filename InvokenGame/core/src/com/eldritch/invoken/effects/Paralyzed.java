package com.eldritch.invoken.effects;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.screens.GameScreen;

public class Paralyzed extends AnimatedEffect {
	private final Agent agent;
	private final float duration;
	
	public Paralyzed(Agent agent, Agent target, float duration) {
		super(target, GameScreen.getRegions("sprite/effects/paralyzed.png", 48, 48)[0],
				Animation.PlayMode.LOOP);
		this.agent = agent;
		this.duration = duration * getDurationScale(agent, target);
	}

	@Override
	public boolean isFinished() {
		return getStateTime() > duration;
	}

	@Override
	public void doApply() {
		getTarget().setParalyzed(agent, true);
	}
	
	@Override
	public void dispel() {
		getTarget().setParalyzed(agent, false);
	}
	
	protected float getDurationScale(Agent agent, Agent target) {
	    return agent.getExecuteScale(target);
	}
}
