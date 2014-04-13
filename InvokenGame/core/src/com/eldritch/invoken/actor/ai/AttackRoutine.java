package com.eldritch.invoken.actor.ai;

import java.util.Map.Entry;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.Agent;
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
	
	private Agent target = null;
	private float elapsed = 0;

	public AttackRoutine(Npc npc) {
		this(npc, 10, 20);
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
	public boolean canInterrupt() {
		return true;
	}

	@Override
	public boolean isValid() {
		return !npc.getEnemies().isEmpty() || shouldAssist();
	}
	
	@Override
	public void reset() {
		elapsed = 0;
	}
	
	private boolean shouldAssist() {
		for (Entry<Agent, Float> entry : npc.getRelations().entrySet()) {
			if (entry.getValue() > 0 && !entry.getKey().getEnemies().isEmpty()) {
				return true;
			}
		}
		return false;
	}
	
	private Agent getEnemy() {
		for (Agent agent : npc.getEnemies()) {
			return agent;
		}
		return null;
	}
	
	private Agent getAllyEnemy() {
		for (Entry<Agent, Float> entry : npc.getRelations().entrySet()) {
			if (entry.getValue() > 0 && !entry.getKey().getEnemies().isEmpty()) {
				for (Agent agent : entry.getKey().getEnemies()) {
					return agent;
				}
			}
		}
		return null;
	}

	@Override
	public void takeAction(float delta, GameScreen screen) {
		// update target enemy
		if (target == null || !target.isAlive()) {
			// get one of our enemies
			target = getEnemy();
			
			// no enemy found, check our allies for enemies
			if (target == null) {
				target = getAllyEnemy();
			}
			
			// can't do anything if we are unable to find a target to attack
			if (target == null || !target.isAlive()) {
				return;
			}
			
			npc.setTarget(target);
		}
		
		move(delta, screen);
		attack(delta, screen);
	}
	
	private void attack(float delta, GameScreen screen) {
		elapsed += delta;
//		Gdx.app.log(InvokenGame.LOG, "elapsed: " + elapsed);
		if (!npc.hasPendingAction() && elapsed >= 1) {
			npc.useAugmentation(0);
			elapsed = 0;
		}
	}
	
	private void move(float delta, GameScreen screen) {
		Vector2 velocityDelta = new Vector2(0, 0);
		if (shouldPursue()) {
			pursueTarget(velocityDelta, screen);
		} else if (shouldFlee()) {
			fleeTarget(velocityDelta, screen);
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

	private void pursueTarget(Vector2 velocity, GameScreen screen) {
		adjustVelocity(npc.getPosition(), getTargetPosition(), velocity, screen);
	}
	
	private void fleeTarget(Vector2 velocity, GameScreen screen) {
		adjustVelocity(getTargetPosition(), npc.getPosition(), velocity, screen);
	}
	
	private void adjustVelocity(Vector2 position, Vector2 target,
			Vector2 velocity, GameScreen screen) {
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

	public Vector2 getTargetPosition() {
		return target.getPosition();
	}

	private boolean shouldPursue() {
		return getTargetPosition().dst2(npc.getPosition()) >= maxDistance;
	}

	private boolean shouldFlee() {
		return getTargetPosition().dst2(npc.getPosition()) <= minDistance;
	}
}
