package com.eldritch.invoken.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.eldritch.invoken.InvokenGame;

public class GameScreen extends AbstractScreen {
	private Player player;

	private TiledMap map;
	private OrthogonalTiledMapRenderer renderer;
	private OrthographicCamera camera;
	private AssetManager assetManager;
	private BitmapFont font;
	private SpriteBatch batch;

	private Pool<Rectangle> rectPool = new Pool<Rectangle>() {
		@Override
		protected Rectangle newObject() {
			return new Rectangle();
		}
	};
	private Array<Rectangle> tiles = new Array<Rectangle>();

	public GameScreen(InvokenGame game) {
		super(game);
	}

	@Override
	public void show() {
		super.show();

		// create an orthographic camera, shows us 10(w/h) x 10 units of the
		// world
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();

		camera = new OrthographicCamera();
		camera.setToOrtho(false, (w / h) * 10, 10);
		camera.zoom = 1.0f;
		camera.update();

		// cameraController = new OrthoCamController(camera);
		// Gdx.input.setInputProcessor(cameraController);

		font = new BitmapFont();
		batch = new SpriteBatch();

		// load the map, set the unit scale to 1/32 (1 unit == 32 pixels)
		assetManager = new AssetManager();
		assetManager.setLoader(TiledMap.class, new TmxMapLoader(
				new InternalFileHandleResolver()));
		assetManager.load("maps/example_woodland.tmx", TiledMap.class);
		assetManager.finishLoading();
		map = assetManager.get("maps/example_woodland.tmx");

		float unitScale = 1 / 32f;
		renderer = new OrthogonalTiledMapRenderer(map, unitScale);

		// create the Player we want to move around the world
		player = new Player();
		player.position.set(20, 15);
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0f / 255f, 0f / 255f, 0f / 255f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// update the player (process input, collision detection, position
		// update)
		player.update(delta, this);

		// let the camera follow the player
		camera.position.x = player.position.x;
		camera.position.y = player.position.y;
		camera.update();

		// set the tile map render view based on what the
		// camera sees and render the map
		renderer.setView(camera);
		renderer.render();

		// render the player
		player.render(delta, renderer);

		batch.begin();
		font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 10, 20);
		batch.end();
	}

	public void getTiles(int startX, int startY, int endX, int endY,
			Array<Rectangle> tiles) {
		TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(1);
		rectPool.freeAll(tiles);
		tiles.clear();
		for (int y = startY; y <= endY; y++) {
			for (int x = startX; x <= endX; x++) {
				Cell cell = layer.getCell(x, y);
				if (cell != null) {
					Rectangle rect = rectPool.obtain();
					rect.set(x, y, 1, 1);
					tiles.add(rect);
				}
			}
		}
	}

	/** The player character, has state and state time, */
	static class Player {
		static float WIDTH;
		static float HEIGHT;
		static float MAX_VELOCITY = 10f;
		static float JUMP_VELOCITY = 40f;
		static float DAMPING = 0.87f;

		private static Texture koalaTexture;
		private static Animation stand;
		private static Animation walk;
		private static Animation jump;

		enum State {
			Standing, Walking, Jumping
		}

		final Vector2 position = new Vector2();
		final Vector2 velocity = new Vector2();
		State state = State.Walking;
		float stateTime = 0;
		boolean facesRight = true;
		boolean facesUp = true;

		static {
			// load the character frames, split them, and assign them to
			// Animations
			koalaTexture = new Texture("sprite/koalio.png");
			TextureRegion[] regions = TextureRegion.split(koalaTexture, 18, 26)[0];
			stand = new Animation(0, regions[0]);
			jump = new Animation(0, regions[1]);
			walk = new Animation(0.15f, regions[2], regions[3], regions[4]);
			walk.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);

			// figure out the width and height of the koala for collision
			// detection and rendering by converting a koala frames pixel
			// size into world units (1 unit == 32 pixels)
			Player.WIDTH = 1 / 32f * regions[0].getRegionWidth();
			Player.HEIGHT = 1 / 32f * regions[0].getRegionHeight();
		}

		public void update(float delta, GameScreen screen) {
			if (delta == 0)
				return;
			stateTime += delta;

			if (Gdx.input.isKeyPressed(Keys.LEFT)
					|| Gdx.input.isKeyPressed(Keys.A) || isTouched(0, 0.25f)) {
				velocity.x = -Player.MAX_VELOCITY;
				state = State.Walking;
				facesRight = false;
			}

			if (Gdx.input.isKeyPressed(Keys.RIGHT)
					|| Gdx.input.isKeyPressed(Keys.D) || isTouched(0.25f, 0.5f)) {
				velocity.x = MAX_VELOCITY;
				state = State.Walking;
				facesRight = true;
			}
			
			if (Gdx.input.isKeyPressed(Keys.UP)
					|| Gdx.input.isKeyPressed(Keys.W) || isTouched(0.75f, 1.0f)) {
				velocity.y = MAX_VELOCITY;
				state = State.Walking;
				facesUp = true;
			}
			
			if (Gdx.input.isKeyPressed(Keys.DOWN)
					|| Gdx.input.isKeyPressed(Keys.S) || isTouched(0.25f, 0.5f)) {
				velocity.y = -MAX_VELOCITY;
				state = State.Walking;
				facesUp = false;
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
			}

			// multiply by delta time so we know how far we go
			// in this frame
			velocity.scl(delta);

			// perform collision detection & response, on each axis, separately
			// if the koala is moving right, check the tiles to the right of
			// it's
			// right bounding box edge, otherwise check the ones to the left
			Rectangle koalaRect = screen.rectPool.obtain();
			koalaRect.set(position.x, position.y, WIDTH, HEIGHT);
			int startX, startY, endX, endY;
			if (velocity.x > 0) {
				startX = endX = (int) (position.x + WIDTH + velocity.x);
			} else {
				startX = endX = (int) (position.x + velocity.x);
			}
			startY = (int) (position.y);
			endY = (int) (position.y + HEIGHT);
			screen.getTiles(startX, startY, endX, endY, screen.tiles);
			koalaRect.x += velocity.x;
			for (Rectangle tile : screen.tiles) {
				if (koalaRect.overlaps(tile)) {
					velocity.x = 0;
					break;
				}
			}
			koalaRect.x = position.x;

			// always check collisions with the bottom of the bounding box
			startY = endY = (int) (position.y + velocity.y);
			startX = (int) (position.x);
			endX = (int) (position.x + WIDTH);
			screen.getTiles(startX, startY, endX, endY, screen.tiles);
			koalaRect.y += velocity.y;
			for (Rectangle tile : screen.tiles) {
				if (koalaRect.overlaps(tile)) {
					velocity.y = 0;
					break;
				}
			}
			koalaRect.y = position.y;
			screen.rectPool.free(koalaRect);

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
			// based on the koala state, get the animation frame
			TextureRegion frame = null;
			switch (state) {
			case Standing:
				frame = stand.getKeyFrame(stateTime);
				break;
			case Walking:
				frame = walk.getKeyFrame(stateTime);
				break;
			case Jumping:
				frame = jump.getKeyFrame(stateTime);
				break;
			}
			
			// draw the koala, depending on the current velocity
			// on the x-axis, draw the koala facing either right
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
}