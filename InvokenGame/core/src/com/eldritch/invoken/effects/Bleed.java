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
		AgentStats sourceStats = source.getStats();
		AgentStats targetStats = getTarget().getStats();
		float chance = 
				sourceStats.getAccuracy()
				* source.getWeaponAccuracy()
				* (1.0f - targetStats.getDefense());
		return Math.random() <= chance;
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
