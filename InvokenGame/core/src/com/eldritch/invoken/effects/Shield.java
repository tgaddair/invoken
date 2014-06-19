package com.eldritch.invoken.effects;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.screens.GameScreen;

public class Shield extends AnimatedEffect {
    private final int magnitude = 50;
    
	public Shield(Agent actor) {
		super(actor, GameScreen.getRegions("sprite/effects/shield.png", 96, 96)[2],
				Animation.PlayMode.LOOP);
	}
	
    @Override
    public void doApply() {
        target.getInfo().modActiveDefense(magnitude);
    }
    
    @Override
    public void dispel() {
        target.getInfo().modActiveDefense(-magnitude);
    }
    
	@Override
	public boolean isFinished() {
		return !getTarget().isToggled(Shield.class);
	}
}
