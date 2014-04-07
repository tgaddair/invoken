package com.eldritch.invoken.actor.ai;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.Npc;

public class FollowRoutine implements Routine {
	private final Npc npc;
	
	/** cannot stray too far from the origin location, or we're wandering, not patrolling */
	private final int maxDistance; // from origin
	private final float maxVelocity;
	private final float duration; // before we update our destination
	private float elapsed;
	
	/** how long we move in a single direction before turning */ 
	private float xVelocity;
	private float yVelocity;
	
	public FollowRoutine(Npc npc) {
		this(npc, npc.getMaxVelocity() * 0.8f, 3, 0.1f);
	}
	
	public FollowRoutine(Npc npc, float maxVelocity, int maxDistance, float duration) {
		this.npc = npc;
		this.maxDistance = maxDistance;
		this.maxVelocity = maxVelocity;
		this.duration = duration;
		elapsed = duration;
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
		elapsed += delta;
		if (!hasStrayed()) {
			xVelocity = yVelocity = 0;
		} else {
			// roughly move towards the origin to get us back on track
			Vector2 position = npc.getPosition();
			Vector2 target = getTarget();
			float dx = target.x - position.x;
			float dy = target.y - position.y;
			
			xVelocity = yVelocity = 0;
			if (Math.abs(Math.abs(dx) - Math.abs(dy)) < 0.1) {
				// prevents flickering when moving along the diagonal
				xVelocity = Math.signum(dx) * maxVelocity;
				yVelocity = Math.signum(dy) * maxVelocity;
			} else if (Math.abs(dx) > Math.abs(dy)) {
				xVelocity = Math.signum(dx) * maxVelocity;
			} else {
				yVelocity = Math.signum(dy) * maxVelocity;
			}
		}
		
		resetVelocity();
	}
	
	private void resetVelocity() {
		npc.setVelocity(xVelocity, yVelocity);
	}
	
	public Vector2 getTarget() {
		return npc.getFollowed().getPosition();
	}
	
	/** returns true if we've strayed too far from the origin of our patrol */
	private boolean hasStrayed() {
		return getTarget().dst2(npc.getPosition()) >= maxDistance;
	}
}
