package com.eldritch.invoken.effects;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.screens.GameScreen;

public class Paralyzed extends AnimatedEffect {
	private final Agent agent;
	private final float duration;
	private boolean applied = false;
	
	public Paralyzed(Agent agent, Agent target, float duration) {
		super(target, GameScreen.getRegions("sprite/effects/paralyzed.png", 48, 48)[0],
				Animation.PlayMode.LOOP);
		this.agent = agent;
		this.duration = duration * agent.getExecuteScale(target);
	}

	@Override
	public boolean isFinished() {
		return getStateTime() > duration;
	}

	@Override
	public void apply(float delta) {
		super.apply(delta);
		if (!applied) {
			getTarget().setParalyzed(agent, true);
			applied = true;
		}
	}
	
	@Override
	public void dispel() {
		getTarget().setParalyzed(agent, false);
	}
}
