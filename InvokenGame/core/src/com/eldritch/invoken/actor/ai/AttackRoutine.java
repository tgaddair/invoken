package com.eldritch.invoken.actor.ai;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.Agent;
import com.eldritch.invoken.actor.Npc;
import com.eldritch.invoken.encounter.Location;

public abstract class AttackRoutine extends MovementRoutine {
    private final Set<Agent> targets = new HashSet<Agent>();
	private Agent target = null;
	private float elapsed = 0;

	public AttackRoutine(Npc npc, Location location) {
	    super(npc, location);
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
	public void reset() {
		elapsed = 0;
		target = null;
	}
	
	protected abstract void fillTargets(Collection<Agent> targets);

	@Override
	public void takeAction(float delta, Location screen) {
		// update target enemy
		if (target == null || !target.isAlive()) {
			// get one of our enemies
		    fillTargets(targets);
		    for (Agent enemy : targets) {
			    target = enemy;
			    break;
		    }
		} else {
			// consider changing targets
			for (Agent agent : npc.getEnemies()) {
				if (npc.dst2(agent) < npc.dst2(target)) {
					// attack the closer enemy
					target = agent;
					npc.setTarget(target);
				}
			}
		}

		npc.setTarget(target);
		if (target == null || !target.isAlive()) {
			// can't do anything if we are unable to find a target to attack
			return;
		}

		move(delta, screen);
		attack(delta, screen);
	}

	private void attack(float delta, Location screen) {
		elapsed += delta;
		if (npc.getTarget() == null || !npc.canTarget()) {
			// can't attack invalid targets
			return;
		}

		// Gdx.app.log(InvokenGame.LOG, "elapsed: " + elapsed);
		if (!npc.hasPendingAction() && elapsed >= 1) {
			npc.useAugmentation(0);
			elapsed = 0;
		}
	}

	@Override
	protected void doMove(Vector2 velocityDelta, Location screen) {
		if (shouldPursue()) {
			pursueTarget(npc.getClearTarget(screen), velocityDelta, screen);
		} else if (shouldFlee()) {
			fleeTarget(getTargetPosition(), velocityDelta, screen);
		}
	}

	public Vector2 getTargetPosition() {
		return target.getPosition();
	}

	private boolean shouldPursue() {
		// don't wait till we've lost them in our sights
		float maxDistance = npc.getInfo().getMaxTargetDistance() * 0.8f;
		return getTargetPosition().dst2(npc.getPosition()) >= maxDistance;
	}

	private boolean shouldFlee() {
		// don't get any closer to the enemy than this
		float minDistance = npc.getInfo().getMaxTargetDistance() * 0.4f;
		return getTargetPosition().dst2(npc.getPosition()) <= minDistance;
	}
}
