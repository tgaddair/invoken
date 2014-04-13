package com.eldritch.invoken.effects;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.eldritch.invoken.actor.Agent;
import com.eldritch.invoken.screens.GameScreen;

public class Shield extends BasicEffect {
	public Shield(Agent actor) {
		super(actor, GameScreen.getRegions("sprite/effects/shield.png", 96, 96)[2],
				Animation.PlayMode.LOOP);
	}
	
	@Override
	public boolean isFinished() {
		return !getOwner().isToggled(Shield.class);
	}
}
