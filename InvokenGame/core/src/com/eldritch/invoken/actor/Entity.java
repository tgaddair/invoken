package com.eldritch.invoken.actor;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.eldritch.invoken.effects.Shield;
import com.eldritch.invoken.screens.GameScreen;

public abstract class Entity {
	static float MAX_VELOCITY = 10f;
	static float JUMP_VELOCITY = 40f;
	static float DAMPING = 0.87f;
	
	enum Direction {
		Up, Left, Down, Right
	}

	enum State {
		Standing, Walking, Jumping
	}

	final Vector2 position = new Vector2();
	final Vector2 velocity = new Vector2();
	State state = State.Walking;
	Animation currentAnim = null;
	float stateTime = 0;
	
	private Shield effect = null;
	
	public Entity(Animation animation, int x, int y) {
		currentAnim = animation;
		setPosition(x, y);
	}
	
	public void toggleShield() {
		if (effect == null) {
			addEffect(new Shield(this));
		} else {
			effect = null;
		}
	}
	
	public void addEffect(Shield effect) {
		this.effect = effect;
	}
	
	public void setPosition(float x, float y) {
		position.set(x, y);
	}
	
	public Vector2 getPosition() {
		return position;
	}
	
	public void setVelocity(float x, float y) {
		velocity.set(x, y);
	}
	
	public Vector2 getVelocity() {
		return velocity;
	}
	
	public float getMaxVelocity() {
		return MAX_VELOCITY;
	}

	public void update(float delta, GameScreen screen) {
		if (delta == 0)
			return;
		stateTime += delta;

		takeAction(delta, screen);

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
					currentAnim = getAnimation(Direction.Left);
				} else {
					// right
					currentAnim = getAnimation(Direction.Right);
				}
			} else {
				if (velocity.y < 0) {
					// down
					currentAnim = getAnimation(Direction.Down);
				} else {
					// up
					currentAnim = getAnimation(Direction.Up);
				}
			}
			state = State.Walking;
		}
		

		// multiply by delta time so we know how far we go
		// in this frame
		velocity.scl(delta);

		// perform collision detection & response, on each axis, separately
		// if the actor is moving right, check the tiles to the right of
		// it's
		// right bounding box edge, otherwise check the ones to the left
		Array<Rectangle> actorRects = getCollisionActors(screen);
		
		float relativeX = position.x - getWidth() / 2;
		float relativeY = position.y - getWidth() / 2;
		
		Rectangle actorRect = GameScreen.getRectPool().obtain();
		getBoundingBox(actorRect);
		
		int startX, startY, endX, endY;
		startX = endX = (int) (relativeX + getWidth() + velocity.x);
		if (velocity.x > 0) {
			startX = endX = (int) (relativeX + getWidth() + velocity.x);
		} else {
			startX = endX = (int) (relativeX + velocity.x);
		}
		startY = (int) (relativeY);
		endY = (int) (relativeY + getHeight());
		screen.getTiles(startX, startY, endX, endY, screen.getTiles());
		
		Array<Rectangle> rects = new Array<Rectangle>();
		rects.addAll(screen.getTiles());
		rects.addAll(actorRects);
		
		float oldX = actorRect.x;
		actorRect.x += velocity.x;
		for (Rectangle tile : rects) {
			if (actorRect.overlaps(tile)) {
				velocity.x = 0;
				break;
			}
		}
		actorRect.x = oldX;

		// always check collisions with the bottom of the bounding box
		if (velocity.y > 0) {
			startX = endX = (int) (relativeY + getHeight() + velocity.y);
		} else {
			startY = endY = (int) (relativeY + velocity.y);
		}
		startX = (int) (relativeX);
		endX = (int) (relativeX + getWidth());
		screen.getTiles(startX, startY, endX, endY, screen.getTiles());
		actorRect.y += velocity.y;
		
		rects.clear();
		rects.addAll(screen.getTiles());
		rects.addAll(actorRects);
		
		for (Rectangle tile : rects) {
			if (actorRect.overlaps(tile)) {
				velocity.y = 0;
				break;
			}
		}
		//actorRect.y = relativeY;
		GameScreen.getRectPool().free(actorRect);
		GameScreen.getRectPool().freeAll(actorRects);

		// unscale the velocity by the inverse delta time and set
		// the latest position
		position.add(velocity);
		velocity.scl(1 / delta);

		// Apply damping to the velocity on the x-axis so we don't
		// walk infinitely once a key was pressed
		velocity.x *= DAMPING;
		velocity.y *= DAMPING;
	}

	public void render(float delta, OrthogonalTiledMapRenderer renderer) {
		// based on the actor state, get the animation frame
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
		
		// draw the actor, depending on the current velocity
		// on the x-axis, draw the actor facing either right
		// or left
		Batch batch = renderer.getSpriteBatch();
		batch.begin();
		batch.draw(frame,
				position.x - getWidth() / 2,
				position.y - getHeight() / 2,
				getWidth(), getHeight());
		batch.end();
		
		if (effect != null) {
			effect.render(delta, renderer);
		}
	}
	
	public Array<Rectangle> getCollisionActors(GameScreen screen) {
		Array<Rectangle> rects = new Array<Rectangle>();
		for (Entity other : screen.getActors()) {
			if (other == this) continue;
			
			// avoid sqrt because it is relatively expensive and unnecessary
			float a = position.x - other.position.x;
			float b = position.y - other.position.y;
			float distance = a * a + b * b;
			
			// our tolerance is the combined radii of both actors
			float w = getWidth() / 2 + other.getWidth() / 2;
			float h = getHeight() / 2 + other.getHeight() / 2;
			float tol = w * w + h * h;
			
			if (distance <= tol) {
				rects.add(other.getBoundingBox(GameScreen.getRectPool().obtain()));
			}
		}
		return rects;
	}
	
	public Rectangle getBoundingBox(Rectangle rect) {
		rect.set(position.x - getWidth() / 8,
				position.y - getHeight() / 2, getWidth() / 4, getHeight() / 4);
		return rect;
	}
	
	protected void setState(State state) {
		this.state = state;
	}
	
	protected abstract void takeAction(float delta, GameScreen screen);
	
	protected abstract float getWidth();
	
	protected abstract float getHeight();
	
	protected abstract Animation getAnimation(Direction dir);
}