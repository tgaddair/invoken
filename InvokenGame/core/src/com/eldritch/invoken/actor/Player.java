package com.eldritch.invoken.actor;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.eldritch.invoken.screens.GameScreen;

/** The player character, has state and state time, */
public class Player extends Actor {
	public static float WIDTH;
	public static float HEIGHT;
	static float MAX_VELOCITY = 10f;
	static float JUMP_VELOCITY = 40f;
	static float DAMPING = 0.87f;

	private static Texture playerTexture;
	private static Map<Direction, Animation> animations;

	static {
		animations = new HashMap<Direction, Animation>();
		
		// load the character frames, split them, and assign them to
		// Animations
		playerTexture = new Texture("sprite/main/walk.png");
		TextureRegion[][] regions = TextureRegion.split(playerTexture, 48, 48);
		
		// up, left, down, right
		for (Direction d : Direction.values()) {
			Animation anim = new Animation(0.15f, regions[d.ordinal()]);
			anim.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
			animations.put(d, anim);
		}

		// figure out the width and height of the player for collision
		// detection and rendering by converting a player frames pixel
		// size into world units (1 unit == 32 pixels)
		Player.WIDTH = 1 / 32f * regions[0][0].getRegionWidth();
		Player.HEIGHT = 1 / 32f * regions[0][0].getRegionHeight();
	}
	
	public Player() {
		super(animations.get(Direction.Down));
		Gdx.input.setInputProcessor(new PlayerInputProcessor());
	}
	
	@Override
	protected void takeAction(float delta, GameScreen screen) {
		if (Gdx.input.isKeyPressed(Keys.LEFT)
				|| Gdx.input.isKeyPressed(Keys.A) || isTouched(0, 0.25f)) {
			velocity.x = -Actor.MAX_VELOCITY;
			setState(State.Walking);
		}

		if (Gdx.input.isKeyPressed(Keys.RIGHT)
				|| Gdx.input.isKeyPressed(Keys.D) || isTouched(0.25f, 0.5f)) {
			velocity.x = MAX_VELOCITY;
			setState(State.Walking);
		}
		
		if (Gdx.input.isKeyPressed(Keys.UP)
				|| Gdx.input.isKeyPressed(Keys.W) || isTouched(0.75f, 1.0f)) {
			velocity.y = MAX_VELOCITY;
			setState(State.Walking);
		}
		
		if (Gdx.input.isKeyPressed(Keys.DOWN)
				|| Gdx.input.isKeyPressed(Keys.S) || isTouched(0.25f, 0.5f)) {
			velocity.y = -MAX_VELOCITY;
			setState(State.Walking);
		}
	}
	
	@Override
	protected float getWidth() {
		return WIDTH;
	}
	
	@Override
	protected float getHeight() {
		return HEIGHT;
	}
	
	@Override
	protected Animation getAnimation(Direction dir) {
		return animations.get(dir);
	}

	private boolean isTouched(float startX, float endX) {
		// check if any finge is touch the area between startX and endX
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
	
	class PlayerInputProcessor implements InputProcessor {
		@Override
		public boolean keyDown(int keycode) {
			return false;
		}

		@Override
		public boolean keyUp(int keycode) {
			if (keycode == Keys.SPACE) {
				toggleShield();
				return true;
			}
			return false;
		}

		@Override
		public boolean keyTyped(char character) {
			return false;
		}

		@Override
		public boolean touchDown(int screenX, int screenY, int pointer,
				int button) {
			return false;
		}

		@Override
		public boolean touchUp(int screenX, int screenY, int pointer, int button) {
			return false;
		}

		@Override
		public boolean touchDragged(int screenX, int screenY, int pointer) {
			return false;
		}

		@Override
		public boolean mouseMoved(int screenX, int screenY) {
			return false;
		}

		@Override
		public boolean scrolled(int amount) {
			return false;
		}
	}
}