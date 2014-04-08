package com.eldritch.invoken.actor.ai;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.Npc;
import com.eldritch.invoken.screens.GameScreen;

public class FollowRoutine implements Routine {
	private final Npc npc;
	
	/** cannot stray too far from the origin location, or we're wandering, not patrolling */
	private final int maxDistance; // from origin
	private final float maxVelocity;
	
	/** how long we move in a single direction before turning */ 
	private final Vector2 velocity = new Vector2();
	
	public FollowRoutine(Npc npc) {
		this(npc, npc.getMaxVelocity() * 1f, 3);
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
	public void takeAction(float delta, GameScreen screen) {
		if (hasStrayed()) {
			Vector2 velocityDelta = new Vector2(0, 0);
			
			// roughly move towards the origin to get us back on track
			followTarget(velocityDelta, screen);
			
			// check that the tile immediately adjacent in the chosen direction is not an obstacle
			avoidCollisions(velocityDelta, screen);
			
			// scale down the previous velocity to reduce the effects of momentum as we're turning
			velocity.scl(0.75f);
			
			// add our velocity delta and clamp it to the max velocity
			velocity.add(velocityDelta);
			bound(velocity, maxVelocity);
		} else {
			velocity.x = velocity.y = 0;
		}
		
		resetVelocity();
	}
	
	private void bound(Vector2 vector, float tol) {
		vector.x = Math.min(Math.max(vector.x, -tol), tol);
		vector.y = Math.min(Math.max(vector.y, -tol), tol);
	}
	
	private void followTarget(Vector2 velocity, GameScreen screen) {
		Vector2 position = npc.getPosition();
		Vector2 target = getTarget();
		float dx = target.x - position.x;
		float dy = target.y - position.y;
		
		if (Math.abs(Math.abs(dx) - Math.abs(dy)) < 0.1) {
			// prevents flickering when moving along the diagonal
			velocity.x += dx;
			velocity.y += dy;
		} else if (Math.abs(dx) > Math.abs(dy)) {
			velocity.x += dx;
		} else {
			velocity.y += dy;
		}
	}
	
	private void avoidCollisions(Vector2 velocity, GameScreen screen) {
		Vector2 position = npc.getPosition().cpy();
		int i1 = (int) position.x - 1;
		int i2 = (int) position.x + 1;
		int j1 = (int) position.y - 1;
		int j2 = (int) position.y + 1;
		
		Vector2 obstacleMass = new Vector2(0, 0);
		int totalObstacles = 0;
		for (int i = i1; i <= i2; i++) {
			for (int j = j1; j <= j2; j++) {
				if (screen.isObstacle(i, j)) {
					obstacleMass.add(i, j);
					totalObstacles++;
				}
			}
		}
		
		if (totalObstacles > 0) {
			obstacleMass.scl(1f / totalObstacles);
			position.sub(obstacleMass).scl(1f);
			Gdx.app.log(InvokenGame.LOG, "force: " + position);
			velocity.add(position);
		}
	}
	
	private void resetVelocity() {
		npc.setVelocity(velocity.x, velocity.y);
	}
	
	public Vector2 getTarget() {
		return npc.getFollowed().getPosition();
	}
	
	/** returns true if we've strayed too far from the origin of our patrol */
	private boolean hasStrayed() {
		return getTarget().dst2(npc.getPosition()) >= maxDistance;
	}
}
