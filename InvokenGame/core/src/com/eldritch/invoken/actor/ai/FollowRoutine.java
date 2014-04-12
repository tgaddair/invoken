package com.eldritch.invoken.actor.ai;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.Npc;
import com.eldritch.invoken.screens.GameScreen;

public class FollowRoutine implements Routine {
	private final Npc npc;
	private final AgentMover mover;
	
	/** teleport to the target at this distance because we're probably stuck */
	private final int maxDistance;
	
	public FollowRoutine(Npc npc) {
		this(npc, npc.getMaxVelocity() * 1f, 3, 20);
	}
	
	public FollowRoutine(Npc npc, float maxVelocity, int minDistance, int maxDistance) {
		this.npc = npc;
		this.maxDistance = maxDistance;
		mover = new AgentMover(npc, maxVelocity, minDistance);
	}
	
	@Override
	public boolean isFinished() {
		return !isValid();
	}
	
	@Override
	public boolean canInterrupt() {
		return true;
	}
	
	@Override
	public boolean isValid() {
		return npc.getFollowed() != null;
	}
	
	@Override
	public void reset() {
		npc.setTarget(null);
	}
	
	@Override
	public void takeAction(float delta, GameScreen screen) {
		if (isStuck()) {
			// TODO teleport to target
		}
		
		mover.takeAction(delta, getTarget(), screen);
	}
	
	public Vector2 getTarget() {
		return npc.getFollowed().getPosition();
	}
	
	private boolean isStuck() {
		return npc.getFollowed().getPosition().dst2(npc.getPosition()) >= maxDistance;
	}
}
