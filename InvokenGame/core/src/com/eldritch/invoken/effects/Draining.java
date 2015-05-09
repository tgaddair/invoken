package com.eldritch.invoken.effects;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.util.Damage;

public class Draining extends AnimatedEffect {
    private final Damage damage;
	private final float duration;
	private boolean cancelled = false;
	
	/**
	 * @param actor caster, the one to be healed
	 * @param target the one to be damaged
	 * @param magnitude damage per second
	 * @param duration seconds of continuous draining
	 */
	public Draining(Agent target, Damage damage, float duration) {
		super(target, GameScreen.getRegions("sprite/effects/draining.png", 48, 48)[0],
				Animation.PlayMode.LOOP);
		this.damage = damage;
		this.duration = duration;
	}
	
	public void cancel() {
	    this.cancelled = true;
	}

	@Override
	public boolean isFinished() {
		return getStateTime() > duration || cancelled;
	}

	@Override
	public void update(float delta) {
		// transfer life essence
		float damaged = getTarget().damage(damage, delta);
		damage.getSource().heal(damaged);
	}
	
	@Override
	public void doApply() {
	}
	
	@Override
	public void dispel() {
	}
}
