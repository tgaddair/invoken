package com.eldritch.invoken.effects;

import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.eldritch.invoken.actor.Agent;

public class Frenzied implements Effect {
	private final Agent master;
	private final Agent target;
	private final float duration;
	private float stateTime = 0;
	private boolean applied = false;
	
	public Frenzied(Agent master, Agent target, float duration) {
		this.master = master;
		this.target = target;
		this.duration = duration;
	}

	@Override
	public boolean isFinished() {
		return stateTime > duration;
	}

	@Override
	public void apply(float delta) {
		if (!applied) {
		    target.setConfused(true);
			applied = true;
		}
		stateTime += delta;
	}
	
	@Override
	public void dispel() {
	    target.setConfused(false);
	}

	@Override
	public void render(float delta, OrthogonalTiledMapRenderer renderer) {
	}
}
