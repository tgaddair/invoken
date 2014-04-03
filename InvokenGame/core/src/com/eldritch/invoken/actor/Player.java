package com.eldritch.invoken.actor;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.screens.GameScreen;

/** The player character, has state and state time, */
public class Player {
	static float WIDTH;
	static float HEIGHT;
	static float MAX_VELOCITY = 10f;
	static float JUMP_VELOCITY = 40f;
	static float DAMPING = 0.87f;

	private static Texture playerTexture;
	private static Map<Direction, Animation> animations;
	
	enum Direction {
		Up, Left, Down, Right
	}

	enum State {
		Standing, Walking, Jumping
	}

	final Vector2 position = new Vector2();
	final Vector2 velocity = new Vector2();
	State state = State.Walking;
	Animation currentAnim = animations.get(Direction.Down);
	float stateTime = 0;
	boolean facesRight = true;
	boolean facesUp = true;

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
	
	public void setPosition(int x, int y) {
		position.set(x, y);
	}
	
	public Vector2 getPosition() {
		return position;
	}

	public void update(float delta, GameScreen screen) {
		if (delta == 0)
			return;
		stateTime += delta;

		if (Gdx.input.isKeyPressed(Keys.LEFT)
				|| Gdx.input.isKeyPressed(Keys.A) || isTouched(0, 0.25f)) {
			velocity.x = -Player.MAX_VELOCITY;
			state = State.Walking;
		}

		if (Gdx.input.isKeyPressed(Keys.RIGHT)
				|| Gdx.input.isKeyPressed(Keys.D) || isTouched(0.25f, 0.5f)) {
			velocity.x = MAX_VELOCITY;
			state = State.Walking;
		}
		
		if (Gdx.input.isKeyPressed(Keys.UP)
				|| Gdx.input.isKeyPressed(Keys.W) || isTouched(0.75f, 1.0f)) {
			velocity.y = MAX_VELOCITY;
			state = State.Walking;
		}
		
		if (Gdx.input.isKeyPressed(Keys.DOWN)
				|| Gdx.input.isKeyPressed(Keys.S) || isTouched(0.25f, 0.5f)) {
			velocity.y = -MAX_VELOCITY;
			state = State.Walking;
		}

		// apply gravity if we are falling
		// velocity.add(0, GRAVITY);

		// clamp the velocity to the maximum
		if (Math.abs(velocity.x) > MAX_VELOCITY) {
			velocity.x = Math.signum(velocity.x) * MAX_VELOCITY;
		}
		
		if (Math.abs(velocity.y) > MAX_VELOCITY) {
			velocity.y = Math.signum(velocity.y) * MAX_VELOCITY;
		}

		// clamp the velocity to 0 if it's < 1, and set the state to
		// standing
		if (Math.abs(velocity.x) < 1 && Math.abs(velocity.y) < 1) {
			velocity.x = 0;
			velocity.y = 0;
			state = State.Standing;
		} else {
			// update the current animation based on the maximal velocity component
			if (Math.abs(velocity.x) > Math.abs(velocity.y)) {
				if (velocity.x < 0) {
					// left
					currentAnim = animations.get(Direction.Left);
				} else {
					// right
					currentAnim = animations.get(Direction.Right);
				}
			} else {
				if (velocity.y < 0) {
					// down
					currentAnim = animations.get(Direction.Down);
				} else {
					// up
					currentAnim = animations.get(Direction.Up);
				}
			}
			state = State.Walking;
		}
		

		// multiply by delta time so we know how far we go
		// in this frame
		velocity.scl(delta);

		// perform collision detection & response, on each axis, separately
		// if the player is moving right, check the tiles to the right of
		// it's
		// right bounding box edge, otherwise check the ones to the left
		Rectangle playerRect = screen.getRectPool().obtain();
		playerRect.set(position.x + WIDTH / 4, position.y + HEIGHT / 4, WIDTH / 2, HEIGHT / 2);
		int startX, startY, endX, endY;
		if (velocity.x > 0) {
			startX = endX = (int) (position.x + WIDTH + velocity.x);
		} else {
			startX = endX = (int) (position.x + velocity.x);
		}
		startY = (int) (position.y);
		endY = (int) (position.y + HEIGHT);
		screen.getTiles(startX, startY, endX, endY, screen.getTiles());
		playerRect.x += velocity.x;
		for (Rectangle tile : screen.getTiles()) {
			if (playerRect.overlaps(tile)) {
				velocity.x = 0;
				break;
			}
		}
		playerRect.x = position.x;

		// always check collisions with the bottom of the bounding box
		startY = endY = (int) (position.y + velocity.y);
		startX = (int) (position.x);
		endX = (int) (position.x + WIDTH);
		screen.getTiles(startX, startY, endX, endY, screen.getTiles());
		playerRect.y += velocity.y;
		for (Rectangle tile : screen.getTiles()) {
			if (playerRect.overlaps(tile)) {
				velocity.y = 0;
				break;
			}
		}
		playerRect.y = position.y;
		screen.getRectPool().free(playerRect);

		// unscale the velocity by the inverse delta time and set
		// the latest position
		position.add(velocity);
		velocity.scl(1 / delta);

		// Apply damping to the velocity on the x-axis so we don't
		// walk infinitely once a key was pressed
		velocity.x *= DAMPING;
		velocity.y *= DAMPING;
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

	public void render(float delta, OrthogonalTiledMapRenderer renderer) {
		// based on the player state, get the animation frame
		TextureRegion frame = null;
		switch (state) {
		case Standing:
			frame = currentAnim.getKeyFrames()[0];
			break;
		case Walking:
			frame = currentAnim.getKeyFrame(stateTime);
			break;
		case Jumping:
			//frame = jump.getKeyFrame(stateTime);
			break;
		}
		
		// draw the player, depending on the current velocity
		// on the x-axis, draw the player facing either right
		// or left
		Batch batch = renderer.getSpriteBatch();
		batch.begin();
		if (facesRight) {
			batch.draw(frame, position.x, position.y, WIDTH, HEIGHT);
		} else {
			batch.draw(frame, position.x + WIDTH, position.y, -WIDTH,
					HEIGHT);
		}
		batch.end();
	}
}