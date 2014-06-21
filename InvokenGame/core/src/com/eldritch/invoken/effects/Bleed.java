package com.eldritch.invoken.effects;

import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.screens.GameScreen;

public class Bleed extends AnimatedEffect {
	private final Agent source;
	private final float magnitude;
	
	public Bleed(Agent actor, Agent target, float magnitude) {
		super(target, GameScreen.getRegions("sprite/effects/bleed.png", 48, 48)[0]);
		this.source = actor;
		this.magnitude = magnitude;
	}
	
	@Override
    protected void doApply() {
        getTarget().damage(source, magnitude);
    }
	
	@Override
    public void dispel() {
    }
}
