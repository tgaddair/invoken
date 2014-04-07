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
	private float xVelocity;
	private float yVelocity;
	
	public FollowRoutine(Npc npc) {
		this(npc, npc.getMaxVelocity() * 0.8f, 3);
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
			
			// check that the tile immediately adjacent in the chosen direction is not an obstacle
			int x1 = (int) npc.getX1();
			int y1 = (int) npc.getY1();
			if (Math.abs(xVelocity) != 0) {
				if (screen.isObstacle((int) (x1 + Math.signum(xVelocity)), y1)) {
					xVelocity = 0;
					yVelocity = Math.signum(dy) * maxVelocity;
				}
			} else if (Math.abs(yVelocity) != 0) {
				if (screen.isObstacle(x1, (int) (y1 + Math.signum(yVelocity)))) {
					xVelocity = Math.signum(dx) * maxVelocity;
					yVelocity = 0;
				}
			}
		} else {
			xVelocity = yVelocity = 0;
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
