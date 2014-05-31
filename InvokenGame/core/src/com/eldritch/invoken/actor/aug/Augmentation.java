package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.graphics.Texture;
import com.eldritch.invoken.actor.Agent;
import com.eldritch.invoken.screens.GameScreen;

public abstract class Augmentation {
    private final Texture icon;
	private int slots;
	private int uses;
	
	public Augmentation(String asset) {
	    this.icon = GameScreen.getTexture("icon/" + asset + ".png");
	}
	
	public void invoke(Agent owner, Agent target) {
		if (isValid(owner, target)) {
			owner.addAction(getAction(owner, target));
		}
	}
	
	public Texture getIcon() {
	    return icon;
	}
	
	public abstract boolean isValid(Agent owner, Agent target);
	
	public abstract Action getAction(Agent owner, Agent target);
}
