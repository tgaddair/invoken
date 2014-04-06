package com.eldritch.invoken.actor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.action.Action;
import com.eldritch.invoken.actor.action.Fire;
import com.eldritch.invoken.actor.weapon.Shotgun;
import com.eldritch.invoken.effects.Bleed;
import com.eldritch.invoken.effects.Effect;
import com.eldritch.invoken.effects.Shield;
import com.eldritch.invoken.screens.GameScreen;

public abstract class AnimatedEntity implements Entity {
	static AssetManager assetManager = new AssetManager();
	static float MAX_VELOCITY = 10f;
	static float JUMP_VELOCITY = 40f;
	static float DAMPING = 0.87f;

	public enum Direction {
		Up, Left, Down, Right
	}

	enum State {
		Standing, Moving
	}

	enum Activity {
		Explore, Combat
	}

	private final float width;
	private final float height;

	final Vector2 position = new Vector2();
	final Vector2 velocity = new Vector2();
	State state = State.Moving;
	Activity activity = Activity.Explore;
	Direction direction = Direction.Down;
	private final Map<Activity, Map<Direction, Animation>> animations =
			new HashMap<Activity, Map<Direction, Animation>>();
	float stateTime = 0;
	
	private final LinkedList<Action> actions = new LinkedList<Action>();
	private final List<Effect> effects = new LinkedList<Effect>();
	private Action action = null;

	private Shotgun weapon;
	private AnimatedEntity target;
	private Shield effect = null;

	public AnimatedEntity(String assetPath, int x, int y) {
		setPosition(x, y);
		animations.put(Activity.Explore, getAnimations(assetPath + "/walk.png"));
		animations.put(Activity.Combat, getAnimations(assetPath + "/shoot.png"));

		// figure out the width and height of the player for collision
		// detection and rendering by converting a player frames pixel
		// size into world units (1 unit == 32 pixels)
		width = 1 / 32f * 48; // regions[0][0].getRegionWidth();
		height = 1 / 32f * 48; // regions[0][0].getRegionHeight();
		
		// for debug purposes
		weapon = new Shotgun(this);
	}
	
	public void attack() {
		if (target != null && target != this) {
			activity = Activity.Combat;
			addAction(new Fire(this));
		}
	}
	
	public void damage(int value) {
		addEffect(new Bleed(this));
	}

	public void toggleShield() {
		if (effect == null) {
			addEffect(new Shield(this));
		} else {
			effect = null;
		}
	}
	
	private void addAction(Action action) {
		actions.add(action);
	}
	
	private void addEffect(Effect effect) {
		effects.add(effect);
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
	
	public Direction getDirection() {
		return direction;
	}

	public Animation getAnimation(Direction direction) {
		return animations.get(activity).get(direction);
	}

	protected void setTarget(AnimatedEntity target) {
		this.target = target;
	}
	
	public AnimatedEntity getTarget() {
		return target;
	}

	public void update(float delta, GameScreen screen) {
		if (delta == 0)
			return;
		stateTime += delta;

		// handle the action queue
		if (action == null || action.isFinished()) {
			action = actions.poll();
			if (action != null) {
				action.apply();
			}
		}
		
		// apply all active effects, remove any that are finished
		Iterator<Effect> it = effects.iterator();
		while (it.hasNext()) {
			Effect effect = it.next();
			if (!effect.isFinished()) {
				effect.apply(delta);
			} else {
				it.remove();
			}
		}
		
		// take conscious action
		takeAction(delta, screen);

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
		} else if (target == null || target == this) {
			// update the current animation based on the maximal velocity
			// component
			direction = getDominantDirection(velocity.x, velocity.y);
			state = State.Moving;
		}

		// do this separately so we can still get the standing state
		if (target != null && target != this) {
			float dx = target.position.x - position.x;
			float dy = target.position.y - position.y;
			direction = getDominantDirection(dx, dy);
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
		// actorRect.y = relativeY;
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

	private Direction getDominantDirection(float x, float y) {
		if (Math.abs(x) > Math.abs(y)) {
			if (x < 0) {
				// left
				return Direction.Left;
			} else {
				// right
				return Direction.Right;
			}
		} else {
			if (y < 0) {
				// down
				return Direction.Down;
			} else {
				// up
				return Direction.Up;
			}
		}
	}

	public void render(float delta, OrthogonalTiledMapRenderer renderer) {
		// based on the actor state, get the animation frame
		TextureRegion frame = null;
		
		Animation animation = getAnimation(direction);
		int index = 0;
		switch (state) {
		case Standing:
			frame = animation.getKeyFrames()[0];
			break;
		case Moving:
			frame = animation.getKeyFrame(stateTime);
			index = animation.getKeyFrameIndex(stateTime);
			break;
		}
		
		// draw the actor, depending on the current velocity
		// on the x-axis, draw the actor facing either right
		// or left
		Batch batch = renderer.getSpriteBatch();
		batch.begin();
		batch.draw(frame, position.x - getWidth() / 2, position.y - getHeight()
				/ 2, getWidth(), getHeight());
		batch.end();
		
		if (activity == Activity.Combat && weapon != null) {
			weapon.render(index, renderer);
		}
		
		// render the current action if one exists
		if (action != null) {
			action.render(delta, renderer);
		}
		
		// render all unfinished effects
		for (Effect effect : effects) {
			if (!effect.isFinished()) {
				effect.render(delta, renderer);
			}
		}

		if (effect != null) {
			effect.render(delta, renderer);
		}
	}

	public Array<Rectangle> getCollisionActors(GameScreen screen) {
		Array<Rectangle> rects = new Array<Rectangle>();
		for (AnimatedEntity other : screen.getActors()) {
			if (other == this)
				continue;

			// avoid sqrt because it is relatively expensive and unnecessary
			float a = position.x - other.position.x;
			float b = position.y - other.position.y;
			float distance = a * a + b * b;

			// our tolerance is the combined radii of both actors
			float w = getWidth() / 2 + other.getWidth() / 2;
			float h = getHeight() / 2 + other.getHeight() / 2;
			float tol = w * w + h * h;

			if (distance <= tol) {
				rects.add(other.getBoundingBox(GameScreen.getRectPool()
						.obtain()));
			}
		}
		return rects;
	}

	public Rectangle getBoundingBox(Rectangle rect) {
		rect.set(position.x - getWidth() / 8, position.y - getHeight() / 2,
				getWidth() / 4, getHeight() / 4);
		return rect;
	}

	public boolean contains(float x, float y) {
		return x >= position.x - getWidth() / 2
				&& x <= position.x + getWidth() / 2
				&& y >= position.y - getHeight() / 2
				&& y <= position.y + getHeight() / 2;
	}

	protected void setState(State state) {
		this.state = state;
	}

	protected abstract void takeAction(float delta, GameScreen screen);

	protected float getWidth() {
		return width;
	}

	protected float getHeight() {
		return height;
	}

	public static Map<Direction, Animation> getAnimations(String assetName) {
		Map<Direction, Animation> animations = new HashMap<Direction, Animation>();

		// up, left, down, right
		TextureRegion[][] regions = GameScreen.getRegions(assetName, 48, 48);
		for (Direction d : Direction.values()) {
			Animation anim = new Animation(0.15f, regions[d.ordinal()]);
			anim.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
			animations.put(d, anim);
		}

		return animations;
	}
}