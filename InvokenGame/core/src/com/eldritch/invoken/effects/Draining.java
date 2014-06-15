package com.eldritch.invoken.effects;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.screens.GameScreen;

public class Draining extends AnimatedEffect {
	private final Agent source;
	private final float magnitude;
	private final float duration;
	
	/**
	 * @param actor caster, the one to be healed
	 * @param target the one to be damaged
	 * @param magnitude damage per second
	 * @param duration seconds of continuous draining
	 */
	public Draining(Agent actor, Agent target, float magnitude, float duration) {
		super(target, GameScreen.getRegions("sprite/effects/draining.png", 48, 48)[0],
				Animation.PlayMode.LOOP);
		this.source = actor;
		this.magnitude = magnitude * actor.getExecuteScale(target);
		this.duration = duration;
	}

	@Override
	public boolean isFinished() {
		return getStateTime() > duration;
	}

	@Override
	public void apply(float delta) {
		super.apply(delta);
		
		// transfer life essence
		Agent target = getTarget();
		float damaged = target.damage(source, magnitude * delta);
		source.heal(damaged);
	}
	
	@Override
	public void dispel() {
	}
}
