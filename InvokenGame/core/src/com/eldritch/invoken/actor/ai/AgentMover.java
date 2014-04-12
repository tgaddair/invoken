package com.eldritch.invoken.actor.ai;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.Agent;
import com.eldritch.invoken.screens.GameScreen;

public class AgentMover {
	private final Agent agent;
	
	/** stop following at this distance to avoid getting in the target's grill */
	private final int minDistance;
	
	/** teleport to the target at this distance because we're probably stuck */
	private final int maxDistance;
	
	private final float maxVelocity;
	
	/** how long we move in a single direction before turning */ 
	private final Vector2 velocity = new Vector2();
	
	private Vector2 target = null;
	
	public AgentMover(Agent agent, float maxVelocity, int minDistance, int maxDistance) {
		this.agent = agent;
		this.minDistance = minDistance;
		this.maxDistance = maxDistance;
		this.maxVelocity = maxVelocity;
	}
	
	public void takeAction(float delta, Vector2 targetCoord, GameScreen screen) {
		target = targetCoord;
		
		if (isStuck()) {
			// TODO teleport to target
		}		
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
			// we're close enough, so stop
			velocity.x = velocity.y = 0;
		}
		
		resetVelocity();
	}
	
	private void bound(Vector2 vector, float tol) {
		vector.x = Math.min(Math.max(vector.x, -tol), tol);
		vector.y = Math.min(Math.max(vector.y, -tol), tol);
	}
	
	private void followTarget(Vector2 velocity, GameScreen screen) {
		Vector2 position = agent.getPosition();
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
		Vector2 position = agent.getPosition().cpy();
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
			velocity.add(position);
		}
	}
	
	private void resetVelocity() {
		agent.setVelocity(velocity.x, velocity.y);
	}
	
	private boolean isStuck() {
		return target.dst2(agent.getPosition()) >= maxDistance;
	}
	
	/** returns true if we've strayed too far from the origin of our patrol */
	private boolean hasStrayed() {
		return target.dst2(agent.getPosition()) >= minDistance;
	}
}
