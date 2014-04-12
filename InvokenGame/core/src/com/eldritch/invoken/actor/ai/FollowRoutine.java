package com.eldritch.invoken.actor.ai;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.Npc;
import com.eldritch.invoken.screens.GameScreen;

public class FollowRoutine implements Routine {
	private final Npc npc;
	private final AgentMover mover;
	
	public FollowRoutine(Npc npc) {
		this(npc, npc.getMaxVelocity() * 1f, 3, 20);
	}
	
	public FollowRoutine(Npc npc, float maxVelocity, int minDistance, int maxDistance) {
		this.npc = npc;
		mover = new AgentMover(npc, maxVelocity, minDistance, maxDistance);
	}
	
	@Override
	public boolean isFinished() {
		return !isValid();
	}
	
	@Override
	public boolean isValid() {
		return npc.getFollowed() != null;
	}
	
	@Override
	public void takeAction(float delta, GameScreen screen) {
		mover.takeAction(delta, getTarget(), screen);
	}
	
	public Vector2 getTarget() {
		return npc.getFollowed().getPosition();
	}
}
