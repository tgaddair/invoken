package com.eldritch.invoken.actor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.eldritch.invoken.screens.GameScreen;

/** The player character, has state and state time, */
public class Player extends AnimatedEntity {
	public Player(int x, int y) {
		super("sprite/main", x, y);
	}
	
	@Override
	protected void takeAction(float delta, GameScreen screen) {
		if (Gdx.input.isKeyPressed(Keys.LEFT)
				|| Gdx.input.isKeyPressed(Keys.A)) {
			velocity.x = -AnimatedEntity.MAX_VELOCITY;
			setState(State.Moving);
		}

		if (Gdx.input.isKeyPressed(Keys.RIGHT)
				|| Gdx.input.isKeyPressed(Keys.D)) {
			velocity.x = AnimatedEntity.MAX_VELOCITY;
			setState(State.Moving);
		}
		
		if (Gdx.input.isKeyPressed(Keys.UP)
				|| Gdx.input.isKeyPressed(Keys.W)) {
			velocity.y = AnimatedEntity.MAX_VELOCITY;
			setState(State.Moving);
		}
		
		if (Gdx.input.isKeyPressed(Keys.DOWN)
				|| Gdx.input.isKeyPressed(Keys.S)) {
			velocity.y = -AnimatedEntity.MAX_VELOCITY;
			setState(State.Moving);
		}
	}
	
	public void select(AnimatedEntity other) {
		setTarget(other);
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