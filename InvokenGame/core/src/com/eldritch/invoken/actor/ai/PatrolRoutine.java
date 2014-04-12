package com.eldritch.invoken.actor.ai;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.Agent;
import com.eldritch.invoken.screens.GameScreen;

public class PatrolRoutine implements Routine {
	private final Agent actor;
	
	/** cannot stray too far from the origin location, or we're wandering, not patrolling */
	private final Vector2 origin;
	private final int maxDistance; // from origin
	private final float maxVelocity;
	
	/** how long we move in a single direction before turning */ 
	private final int duration;
	private float elapsed = 0;
	private float xVelocity;
	private float yVelocity;
	
	public PatrolRoutine(Agent actor) {
		this(actor, actor.getMaxVelocity() / 4, 10, 3);
	}
	
	public PatrolRoutine(Agent actor, float maxVelocity, int maxDistance, int duration) {
		this.actor = actor;
		this.origin = actor.getPosition().cpy();
		this.maxDistance = maxDistance;
		this.maxVelocity = maxVelocity;
		this.duration = duration;
	}
	
	@Override
	public boolean isFinished() {
		return elapsed >= duration;
	}
	
	@Override
	public boolean canInterrupt() {
		return false;
	}
	
	@Override
	public boolean isValid() {
		return Math.random() < 0.5;
	}
	
	@Override
	public void reset() {
		elapsed = 0;
		actor.setTarget(null);
	}
	
	@Override
	public void takeAction(float delta, GameScreen screen) {
		// try to move in some direction until time is up or we hit an obstacle
		Vector2 velocity = actor.getVelocity();
		if (velocity.isZero() || elapsed >= duration) {
			// we're not moving, so pick a new direction and start walking
			float x = 0;
			float y = 0;
			
			if (heads()) {
				// move up-down
				x = heads() ? 1 : -1;
			} else {
				// move left-right
				y = heads() ? 1 : -1;
			}
			
			elapsed = 0;
			xVelocity = x * maxVelocity;
			yVelocity = y * maxVelocity;
		} else if (hasStrayed()) {
			// roughly move towards the origin to get us back on track
			Vector2 position = actor.getPosition();
			float dx = origin.x - position.x;
			float dy = origin.y - position.y;
			
			xVelocity = yVelocity = 0;
			if (Math.abs(dx) > Math.abs(dy)) {
				xVelocity = Math.signum(dx) * maxVelocity;
			} else {
				yVelocity = Math.signum(dy) * maxVelocity;
			}
		} else {
			// keep going in our patrol direction
			elapsed += delta;
		}
		
		resetVelocity();
	}
	
	private void resetVelocity() {
		actor.setVelocity(xVelocity, yVelocity);
	}
	
	/** returns true if we've strayed too far from the origin of our patrol */
	private boolean hasStrayed() {
		return origin.dst2(actor.getPosition()) >= maxDistance;
	}
	
	/** returns true if the toss of a fair coin lands heads, false if tails */
	private boolean heads() {
		return Math.random() > 0.5;
	}
}
