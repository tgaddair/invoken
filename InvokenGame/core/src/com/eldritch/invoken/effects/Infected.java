package com.eldritch.invoken.effects;

import java.util.Set;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.util.Damage;

public class Infected extends AnimatedEffect {
	private static final float INCUBATION_PERIOD = 0.5f;
	
	private final Set<Agent> immune;
	private final Damage damage;
	private final float duration;
	private final float radius;
	
	/**
	 * @param actor caster, the one to be healed
	 * @param target the one to be damaged
	 * @param magnitude damage per second
	 * @param duration seconds of continuous infection
	 */
	public Infected(Agent target, Set<Agent> immune,
			Damage damage, float duration, float radius) {
		super(target, GameScreen.getRegions("sprite/effects/draining.png", 48, 48)[0],
				Animation.PlayMode.LOOP);
		this.immune = immune;
		this.damage = damage;
		this.duration = duration * damage.getSource().getExecuteScale(target);
		this.radius = radius;
	}

	@Override
	public boolean isFinished() {
		return getStateTime() > duration;
	}

	@Override
	public void update(float delta) {
		Agent target = getTarget();
		target.damage(damage, delta);
		
		// spread to neighbors only when we're past the incubation period
		if (getStateTime() < INCUBATION_PERIOD) {
			return;
		}
		
		// spread to neighbors
		for (Agent neighbor : target.getNeighbors()) {
			if (!immune.contains(neighbor) && !neighbor.isToggled(Infected.class)) {
				// infection does not stack
				if (neighbor.inRange(target.getPosition(), radius)) {
					// use the scaled magnitudes, durations, and radii so that the further separated
					// victims are from the source, the less damage they incur
					immune.add(neighbor);
					neighbor.addEffect(
							new Infected(neighbor, immune, damage, duration, radius));
	    		}
			}
    	}
	}
	
	@Override
	public void doApply() {
		target.toggleOn(Infected.class);
	}
	
	@Override
	public void dispel() {
		target.toggleOff(Infected.class);
	}
}
