package com.eldritch.invoken.actor.ai;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.Npc;

public class FollowRoutine implements Routine {
	private final Npc npc;
	
	/** cannot stray too far from the origin location, or we're wandering, not patrolling */
	private final int maxDistance; // from origin
	private final float maxVelocity;
	
	/** how long we move in a single direction before turning */ 
	private float xVelocity;
	private float yVelocity;
	
	public FollowRoutine(Npc npc) {
		this(npc, npc.getMaxVelocity() / 4, 5);
	}
	
	public FollowRoutine(Npc npc, float maxVelocity, int maxDistance) {
		this.npc = npc;
		this.maxDistance = maxDistance;
		this.maxVelocity = maxVelocity;
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
	public void takeAction(float delta) {
		if (hasStrayed()) {
			// move towards the origin to get us back on track
			Vector2 origin = getTarget();
			Vector2 position = npc.getPosition();
			float dx = origin.x - position.x;
			float dy = origin.y - position.y;
			xVelocity = Math.signum(dx) * maxVelocity;
			yVelocity = Math.signum(dy) * maxVelocity;
		}
		
		resetVelocity();
	}
	
	private void resetVelocity() {
		npc.setVelocity(xVelocity, yVelocity);
	}
	
	private Vector2 getTarget() {
		return npc.getFollowed().getPosition();
	}
	
	/** returns true if we've strayed too far from the origin of our patrol */
	private boolean hasStrayed() {
		return getTarget().dst2(npc.getPosition()) >= maxDistance;
	}
}
