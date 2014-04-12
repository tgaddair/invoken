package com.eldritch.invoken.actor.ai;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.Npc;
import com.eldritch.invoken.screens.GameScreen;

public class AttackRoutine implements Routine {
	private final Npc npc;

	/** don't get any close to the enemy than this */
	private final int minDistance;

	/** pursue the enemy if they're farther away than this */
	private final int maxDistance;

	/** how long we move in a single direction before turning */
	private final Vector2 velocity = new Vector2();

	public AttackRoutine(Npc npc) {
		this(npc, 3, 20);
	}

	public AttackRoutine(Npc npc, int minDistance, int maxDistance) {
		this.npc = npc;
		this.minDistance = minDistance;
		this.maxDistance = maxDistance;
	}

	@Override
	public boolean isFinished() {
		return !isValid();
	}

	@Override
	public boolean isValid() {
		return npc.getTarget() != null; // && npc.isEnemy(npc.getTarget())
	}

	@Override
	public void takeAction(float delta, GameScreen screen) {
		Vector2 velocityDelta = new Vector2(0, 0);

		if (shouldPursue()) {
			// roughly move towards the origin to get us back on track
			followTarget(velocityDelta, screen);
		} else if (shouldFlee()) {

		}

		// check that the tile immediately adjacent in the chosen direction is
		// not an obstacle
		avoidCollisions(velocityDelta, screen);

		// scale down the previous velocity to reduce the effects of momentum as
		// we're turning
		velocity.scl(0.75f);

		// add our velocity delta and clamp it to the max velocity
		velocity.add(velocityDelta);
		bound(velocity, npc.getMaxVelocity());

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
			velocity.add(position);
		}
	}

	private void resetVelocity() {
		npc.setVelocity(velocity.x, velocity.y);
	}

	public Vector2 getTarget() {
		return npc.getTarget().getPosition();
	}

	private boolean shouldPursue() {
		return getTarget().dst2(npc.getPosition()) >= maxDistance;
	}

	private boolean shouldFlee() {
		return getTarget().dst2(npc.getPosition()) <= minDistance;
	}
}