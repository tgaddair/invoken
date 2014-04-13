package com.eldritch.invoken.effects;

import com.eldritch.invoken.actor.Agent;
import com.eldritch.invoken.screens.GameScreen;

public class Bleed extends BasicEffect {
	public Bleed(Agent actor) {
		super(actor, GameScreen.getRegions("sprite/effects/bleed.png", 48, 48)[0]);
	}
}
