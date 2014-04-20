package com.eldritch.invoken.effects;

import com.eldritch.invoken.actor.Agent;
import com.eldritch.invoken.actor.AgentStats;
import com.eldritch.invoken.screens.GameScreen;

public class Bleed extends BasicEffect {
	private final Agent source;
	private final float magnitude;
	private boolean applied = false;
	
	public Bleed(Agent actor, Agent target, float magnitude) {
		super(target, GameScreen.getRegions("sprite/effects/bleed.png", 48, 48)[0]);
		this.source = actor;
		this.magnitude = magnitude;
	}
	
	@Override
	public boolean succeeds() {
		return Math.random() <= source.getAttackChance(getTarget());
	}
	
	@Override
	public void apply(float delta) {
		super.apply(delta);
		if (!applied) {
			getTarget().damage(source, magnitude);
			applied = true;
		}
	}
}
