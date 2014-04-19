package com.eldritch.invoken.effects;

import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.eldritch.invoken.actor.Agent;

public class Paralyzed implements Effect {
	private final Agent invoker;
	private final Agent target;
	private final float duration;
	private float stateTime = 0;
	private boolean applied = false;
	
	public Paralyzed(Agent invoker, Agent target, float duration) {
		this.invoker = invoker;
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
			target.setParalyzed(true);
			applied = true;
		}
		stateTime += delta;
	}
	
	@Override
	public void dispel() {
		target.setParalyzed(false);
	}

	@Override
	public void render(float delta, OrthogonalTiledMapRenderer renderer) {
	}
}
