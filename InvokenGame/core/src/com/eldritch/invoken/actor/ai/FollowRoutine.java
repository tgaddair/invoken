package com.eldritch.invoken.actor.ai;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.Agent;
import com.eldritch.invoken.actor.Npc;
import com.eldritch.invoken.encounter.Location;

public class FollowRoutine implements Routine {
	private final Npc npc;
	private final AgentMover mover;
	
	/** teleport to the target at this distance because we're probably stuck */
	private final int maxDistance;
	
	public FollowRoutine(Npc npc) {
		this(npc, npc.getMaxVelocity() * 1f, 3, 20);
	}
	
	public FollowRoutine(final Npc npc, float maxVelocity, int minDistance, int maxDistance) {
		this.npc = npc;
		this.maxDistance = maxDistance;
		mover = new AgentMover(npc, maxVelocity, minDistance) {
			protected void move(Vector2 velocity, Location screen) {
				if (npc.getFollowed() == null) {
					return;
				}
				
				Vector2 position = npc.getPosition().cpy();
				Vector2 mass = new Vector2(0, 0);
				int n = 0;
				for (Agent agent : npc.getFollowed().getFollowers()) {
					if (agent != npc && position.dst2(agent.getPosition()) < 1) {
						mass.add(agent.getPosition());
						n++;
					}
				}
				
				if (n > 0) {
					mass.scl(1f / n);
					position.sub(mass).scl(2f);
					velocity.add(position);
				}
			}
		};
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
	public void takeAction(float delta, Location screen) {
		if (isStuck()) {
			// TODO teleport to target
		}
		
		mover.takeAction(delta, npc.getClearTarget(screen), screen);
	}
	
	public Vector2 getTarget() {
		return npc.getFollowed().getPosition();
	}
	
	private boolean isStuck() {
		return npc.getFollowed().getPosition().dst2(npc.getPosition()) >= maxDistance;
	}
}
