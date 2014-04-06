package com.eldritch.invoken.actor;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.eldritch.invoken.screens.GameScreen;

/** The player character, has state and state time, */
public class Player extends AnimatedEntity {
	public static float WIDTH;
	public static float HEIGHT;

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
	
	public Player(int x, int y) {
		super(animations.get(Direction.Down), x, y);
	}
	
	@Override
	protected void takeAction(float delta, GameScreen screen) {
		if (Gdx.input.isKeyPressed(Keys.LEFT)
				|| Gdx.input.isKeyPressed(Keys.A)) {
			velocity.x = -AnimatedEntity.MAX_VELOCITY;
			setState(State.Walking);
		}

		if (Gdx.input.isKeyPressed(Keys.RIGHT)
				|| Gdx.input.isKeyPressed(Keys.D)) {
			velocity.x = AnimatedEntity.MAX_VELOCITY;
			setState(State.Walking);
		}
		
		if (Gdx.input.isKeyPressed(Keys.UP)
				|| Gdx.input.isKeyPressed(Keys.W)) {
			velocity.y = AnimatedEntity.MAX_VELOCITY;
			setState(State.Walking);
		}
		
		if (Gdx.input.isKeyPressed(Keys.DOWN)
				|| Gdx.input.isKeyPressed(Keys.S)) {
			velocity.y = -AnimatedEntity.MAX_VELOCITY;
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