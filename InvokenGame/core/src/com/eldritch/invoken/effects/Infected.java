package com.eldritch.invoken.effects;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.screens.GameScreen;

public class Infected extends AnimatedEffect {
	private final Agent source;
	private final float magnitude;
	private final float duration;
	private final float radius;
	
	/**
	 * @param actor caster, the one to be healed
	 * @param target the one to be damaged
	 * @param magnitude damage per second
	 * @param duration seconds of continuous infection
	 */
	public Infected(Agent agent, Agent target, float magnitude, float duration, float radius) {
		super(target, GameScreen.getRegions("sprite/effects/draining.png", 48, 48)[0],
				Animation.PlayMode.LOOP);
		this.source = agent;
		this.magnitude = magnitude * agent.getExecuteScale(target);
		this.duration = duration * agent.getExecuteScale(target);
		this.radius = radius;
	}

	@Override
	public boolean isFinished() {
		return getStateTime() > duration;
	}

	@Override
	public void update(float delta) {
		Agent target = getTarget();
		target.damage(source, magnitude * delta);
		
		// spread to neighbors
		for (Agent neighbor : target.getNeighbors()) {
			if (!neighbor.isToggled(Infected.class)) {
				// infection does not stack
				if (neighbor.inRange(target.getPosition(), radius)) {
					// use the scaled magnitudes, durations, and radii so that the further separated
					// victims are from the source, the less damage they incur
					neighbor.addEffect(new Infected(source, neighbor, magnitude, duration, radius));
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
