package com.eldritch.invoken.actor.ai;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.Agent;
import com.eldritch.invoken.actor.Npc;
import com.eldritch.invoken.encounter.Location;

public class FleeRoutine extends MovementRoutine {
	private final Set<Agent> targets = new HashSet<Agent>();
	private Agent target = null;
	private float elapsed = 0;

	public FleeRoutine(Npc npc) {
		super(npc);
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
		return npc.getBehavior().shouldFlee();
	}

	@Override
	public void reset() {
		elapsed = 0;
		target = null;
	}
	
	private boolean targetIsInvalid() {
	    return !target.isAlive() || !targets.contains(target);
	}

	@Override
	public void takeAction(float delta, Location location) {
	    // update valid flee targets
	    npc.getBehavior().getFleeTargets(location.getActors(), targets);
	    
	    // choose a target to flee from
	    if (target == null || targetIsInvalid()) {
	        target = null;
	        for (Agent agent : targets) {
	            if (target == null || agent.getInfo().getLevel() > target.getInfo().getLevel()) {
	                // flee from the highest level target first
	                target = agent;
	            }
	        }
	    }

	    if (target == null) {
			// can't do anything if we are unable to find a target to attack
			return;
		}

		move(delta, location);
	}

	@Override
	protected void doMove(Vector2 velocityDelta, Location location) {
		if (shouldFlee()) {
		    Vector2 destination = npc.getPathfinder().rotate(
		            target.getPosition(), npc.getPosition(), Math.PI);
			pursueTarget(
			        npc.getPathfinder().getTarget(npc.getPosition(), destination, location),
			        velocityDelta, location);
		}
	}

	public Vector2 getTargetPosition() {
		return target.getPosition();
	}

	private boolean shouldFlee() {
		// don't get any closer to the enemy than this
		float minDistance = target.getInfo().getMaxTargetDistance() * 0.4f;
		return getTargetPosition().dst2(npc.getPosition()) <= minDistance;
	}
}
