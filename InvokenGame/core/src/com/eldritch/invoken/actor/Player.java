package com.eldritch.invoken.actor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.ai.AgentMover;
import com.eldritch.invoken.encounter.Location;

/** The player character, has state and state time, */
public class Player extends Agent {
	private final AgentMover mover;
	private final Vector2 targetCoord = new Vector2();
	private boolean moving = false;
	
	public Player(Profession profession, int level, float x, float y, String body) {
		super(body, x, y, profession, level);
		mover = new AgentMover(this, getMaxVelocity(), 0.01f);
	}
	
	@Override
	protected void takeAction(float delta, Location screen) {
		if (moving) {
			moving = mover.takeAction(delta, targetCoord, screen);
		}
		
		if (Gdx.input.isKeyPressed(Keys.LEFT)
				|| Gdx.input.isKeyPressed(Keys.A)) {
			velocity.x = -Agent.MAX_VELOCITY;
			moving = false;
		}

		if (Gdx.input.isKeyPressed(Keys.RIGHT)
				|| Gdx.input.isKeyPressed(Keys.D)) {
			velocity.x = Agent.MAX_VELOCITY;
			moving = false;
		}
		
		if (Gdx.input.isKeyPressed(Keys.UP)
				|| Gdx.input.isKeyPressed(Keys.W)) {
			velocity.y = Agent.MAX_VELOCITY;
			moving = false;
		}
		
		if (Gdx.input.isKeyPressed(Keys.DOWN)
				|| Gdx.input.isKeyPressed(Keys.S)) {
			velocity.y = -Agent.MAX_VELOCITY;
			moving = false;
		}
	}
	
	public void moveTo(float x, float y) {
		targetCoord.x = x;
		targetCoord.y = y;
	}
	
	public void setMoving(boolean moving) {
		this.moving = moving;
	}
	
	public boolean isMoving() {
		return moving;
	}
	
	public void select(Agent other) {
		setTarget(other);
		interact(null);
	}
	
	public void reselect(Agent other) {
		other.handleInteract(this);
	}
	
	@Override
	public void setConfused(boolean confused) {
	    // do nothing, for now, will change to make attack at random
	}
	
	@Override
	public void handleInteract(Agent other) {
		// do nothing
	}

	private boolean isTouched(float startX, float endX) {
		// check if any finger is touch the area between startX and endX
		// startX/endX are given between 0 (left edge of the screen) and 1
		// (right edge of the screen)
		for (int i = 0; i < 2; i++) {
			float x = Gdx.input.getX() / (float) Gdx.graphics.getWidth();
			if (Gdx.input.isTouched(i) && (x >= startX && x <= endX)) {
				return true;
			}
		}
		return false;
	}
}