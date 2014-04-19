package com.eldritch.invoken.effects;

import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.eldritch.invoken.actor.Agent;

public class Commanded implements Effect {
	private final Agent master;
	private final Agent target;
	private final float duration;
	private float stateTime = 0;
	private boolean applied = false;
	
	public Commanded(Agent master, Agent target, float duration) {
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
			master.addFollower(target);
			applied = true;
		}
		stateTime += delta;
	}
	
	@Override
	public void dispel() {
		master.removeFollower(target);
	}

	@Override
	public void render(float delta, OrthogonalTiledMapRenderer renderer) {
	}
}
