package com.eldritch.invoken.screens;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
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
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.Agent;
import com.eldritch.invoken.actor.Npc;
import com.eldritch.invoken.actor.Player;

public class GameScreen extends AbstractScreen implements InputProcessor {
	public static final AssetManager textureManager = new AssetManager();
	
	private static Pool<Rectangle> rectPool = new Pool<Rectangle>() {
		@Override
		protected Rectangle newObject() {
			return new Rectangle();
		}
	};

	private Player player;
	private final List<Agent> entities = new ArrayList<Agent>();

	private TiledMap map;
	private TextureRegion selector;
	private OrthogonalTiledMapRenderer renderer;
	private OrthographicCamera camera;
	private AssetManager assetManager;
	private BitmapFont font;
	private SpriteBatch batch;
	private Array<Rectangle> tiles = new Array<Rectangle>();

	public GameScreen(InvokenGame game) {
		super(game);
	}

	public static Pool<Rectangle> getRectPool() {
		return rectPool;
	}

	public Array<Rectangle> getTiles() {
		return tiles;
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
		
		// load the selector
		selector = new TextureRegion(new Texture("sprite/selection.png"));

		// create the Player we want to move around the world
		player = new Player(20, 15);
		addActor(player);
		addActor(new Npc(25, 15));
		addActor(new Npc(27, 20));
		addActor(new Npc(27, 10));
		
		Gdx.input.setInputProcessor(this);
		
		Gdx.app.log(InvokenGame.LOG, "start");
	}

	private void addActor(Agent actor) {
		entities.add(actor);
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0f / 255f, 0f / 255f, 0f / 255f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// update the player (process input, collision detection, position
		// update)
		for (Agent actor : entities) {
			actor.update(delta, this);
		}

		// let the camera follow the player
		Vector2 position = player.getPosition();
		camera.position.x = position.x;
		camera.position.y = position.y;
		camera.update();

		// set the tile map render view based on what the
		// camera sees and render the map
		renderer.setView(camera);
		renderer.render();

		// render the player
		for (Agent actor : entities) {
			if (actor == player.getTarget()) {
				Color color = new Color(0x00FA9AFF);
				if (!actor.isAlive()) {
					// dark slate blue
					color = new Color(0x483D8BFF);
				}
				drawCentered(selector, actor.getPosition(), color);
			}
			actor.render(delta, renderer);
		}

		batch.begin();
		font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 10, 20);
		batch.end();
	}
	
	private void drawCentered(TextureRegion region, Vector2 position, Color color) {
		float w = 1 / 32f * region.getRegionWidth();
		float h = 1 / 32f * region.getRegionHeight();
		
		Batch batch = renderer.getSpriteBatch();
		batch.setColor(color);
		batch.begin();
		batch.draw(region, position.x - w / 2, position.y - h / 2 - 0.2f, w, h);
		batch.end();
		batch.setColor(Color.WHITE);
	}

	public List<Agent> getActors() {
		return entities;
	}
	
	public boolean isObstacle(int x, int y) {
		TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(1);
		return layer.getCell(x, y) != null;
	}

	public Array<Rectangle> getTiles(int startX, int startY, int endX,
			int endY, Array<Rectangle> tiles) {
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
		return tiles;
	}

	@Override
	public boolean keyDown(int keycode) {
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		switch (keycode) {
		case Keys.NUM_1:
			player.useAugmentation(0);
			return true;
		case Keys.NUM_2:
			player.useAugmentation(1);
			return true;
		case Keys.NUM_3:
			player.useAugmentation(2);
			return true;
		default:
			return false;
		}
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		Vector3 world = camera.unproject(new Vector3(screenX, screenY, 0));
		for (Agent entity : entities) {
			if (entity.contains(world.x, world.y)) {
				return false;
			}
		}
		
		// otherwise, move to the indicated position
		player.moveTo(world.x, world.y);
		return true;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		Vector3 world = camera.unproject(new Vector3(screenX, screenY, 0));
		for (Agent entity : entities) {
			if (entity.contains(world.x, world.y)) {
				// toggle selection
				Agent selected = player.getTarget() != entity ? entity : null;
				player.select(selected);
				return true;
			}
		}
		
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		if (player.isMoving()) {
			Vector3 world = camera.unproject(new Vector3(screenX, screenY, 0));
			player.moveTo(world.x, world.y);
			return true;
		}
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
	
	
	public static TextureRegion[][] getRegions(String assetName, int w, int h) {
		// load the character frames, split them, and assign them to
		// Animations
		if (!textureManager.isLoaded(assetName, Texture.class)) {
			textureManager.load(assetName, Texture.class);
			textureManager.finishLoading();
		}
		Texture texture = textureManager.get(assetName, Texture.class);
		return TextureRegion.split(texture, w, h);
	}
}