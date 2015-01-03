package com.eldritch.invoken.effects;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.screens.GameScreen;

public class Frozen extends AnimatedEffect {
	private final float magnitude;
	private final float duration;
	
	public Frozen(Agent master, Agent target, float magnitude, float duration) {
	    super(target, GameScreen.getMergedRegion("sprite/effects/frost-bubbles.png", 96, 96),
	    		new Vector2(0, -0.2f));
	    this.magnitude = magnitude;
		this.duration = duration;
	}
	
	@Override
    protected void doApply() {
		target.freeze(magnitude);
    }

	@Override
	public void dispel() {
		target.freeze(-magnitude);
	}
}
