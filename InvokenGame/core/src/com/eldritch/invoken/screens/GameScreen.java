package com.eldritch.invoken.screens;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
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
import com.eldritch.invoken.actor.Profession.Inquisitor;
import com.eldritch.invoken.actor.factions.Faction;
import com.eldritch.invoken.ui.DialogueMenu;
import com.eldritch.invoken.ui.InventoryMenu;
import com.eldritch.invoken.ui.LootMenu;
import com.google.common.primitives.Ints;

public class GameScreen extends AbstractScreen implements InputProcessor {
	public static final AssetManager textureManager = new AssetManager();
	
	private static Pool<Rectangle> rectPool = new Pool<Rectangle>() {
		@Override
		protected Rectangle newObject() {
			return new Rectangle();
		}
	};
	
	private final DialogueMenu dialogue;
	private final LootMenu loot;
	
	private InventoryMenu inventoryMenu;

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
	
	private int[] overlays;
	private int collisionIndex;
	
	// for clicks and drags
	boolean playerClicked = false;
	int targetX;
	int targetY;

	public GameScreen(InvokenGame game) {
		super(game);
		dialogue = new DialogueMenu(getSkin());
		loot = new LootMenu(getSkin());
		stage.addActor(dialogue.getTable());
		stage.addActor(loot.getTable());
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
		camera.zoom = 1.5f;
		camera.update();

		font = new BitmapFont();
		batch = new SpriteBatch();

		// load the map, set the unit scale to 1/32 (1 unit == 32 pixels)
		String mapAsset = "maps/underground.tmx";
		assetManager = new AssetManager();
		assetManager.setLoader(TiledMap.class, new TmxMapLoader(
				new InternalFileHandleResolver()));
		assetManager.load(mapAsset, TiledMap.class);
		assetManager.finishLoading();
		map = assetManager.get(mapAsset);
		
		// find layers we care about
		List<Integer> overlayList = new ArrayList<Integer>();
		for (int i = 0; i < map.getLayers().getCount(); i++) {
			TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(i);
			if (layer.getName().equals("collision")) {
				layer.setVisible(false);
				collisionIndex = i;
			} else if (layer.getName().equals("overlay") || layer.getName().equals("trim")) {
				// overlays are rendered above all objects always
				overlayList.add(i);
			}
		}
		overlays = Ints.toArray(overlayList);
		
		// objects are rendered by y-ordering with other entities
		
		float unitScale = 1 / 32f;
		renderer = new OrthogonalTiledMapRenderer(map, unitScale);
		
		// load the selector
		selector = new TextureRegion(new Texture("sprite/selection.png"));
		
		// debug factions
		Faction playerFaction = new Faction("Player");
		Faction eruFaction = new Faction("House Eru");
		Faction imperialFaction = new Faction("Icarian Empire");
		Faction gangFaction = new Faction("Gang");
		playerFaction.addRelation(gangFaction, -10);
		eruFaction.addRelation(gangFaction, -50);
		imperialFaction.addRelation(gangFaction, -50);
		gangFaction.addRelation(playerFaction, -50);
		gangFaction.addRelation(imperialFaction, -50);
		gangFaction.addRelation(eruFaction, -50);

		// create the Player we want to move around the world
		Vector2 spawn = getSpawnLocation();
		player = new Player(new Inquisitor(), 10, spawn.x, spawn.y);
		player.addFaction(playerFaction, 9, 0);
		addActor(player);
		
		// create player menus
		inventoryMenu = new InventoryMenu(player, getSkin());
		stage.addActor(inventoryMenu.getTable());
		
		// find spawn nodes
		LinkedList<Vector2> spawnNodes = getSpawnNodes();
		
		// create test NPCs
		String asset = "sprite/characters/male-fair.png";
		addActor(createTestNpc(spawnNodes.poll(), "QuarantineEruSentry", asset));
		addActor(createTestNpc(spawnNodes.poll(), "EruCenturion", asset));
		
		addActor(createTestNpc(spawnNodes.poll(), "InquisitorGoth", asset));
		
		addActor(createTestNpc(spawnNodes.poll(), "IcarianEnforcer", asset));
		addActor(createTestNpc(spawnNodes.poll(), "IcarianEnforcer", asset));
		addActor(createTestNpc(spawnNodes.poll(), "IcarianEnforcer", asset));
		
		addActor(createTestNpc(spawnNodes.poll(), "StreetThug", asset));
		addActor(createTestNpc(spawnNodes.poll(), "StreetThug", asset));
		addActor(createTestNpc(spawnNodes.poll(), "StreetThug", asset));
		addActor(createTestNpc(spawnNodes.poll(), "StreetThug", asset));
		addActor(createTestNpc(spawnNodes.poll(), "StreetThug", asset));
		addActor(createTestNpc(spawnNodes.poll(), "StreetThug", asset));

		Gdx.input.setInputProcessor(this);
		Gdx.app.log(InvokenGame.LOG, "start");
	}
	
	private LinkedList<Vector2> getSpawnNodes() {
		LinkedList<Vector2> list = new LinkedList<Vector2>();
		TiledMapTileLayer spawnLayer = (TiledMapTileLayer) map.getLayers().get("spawn");
		spawnLayer.setVisible(false);
		for (int x = 0; x < spawnLayer.getWidth(); x++) {
			for (int y = 0; y < spawnLayer.getHeight(); y++) {
				Cell cell = spawnLayer.getCell(x, y);
				if (cell != null) {
					list.add( new Vector2(x, y));
				}
			}
		}
		return list;
	}
	
	private Vector2 getSpawnLocation() {
		TiledMapTileLayer spawnLayer = (TiledMapTileLayer) map.getLayers().get("player");
		spawnLayer.setVisible(false);
		for (int x = 0; x < spawnLayer.getWidth(); x++) {
			for (int y = 0; y < spawnLayer.getHeight(); y++) {
				Cell cell = spawnLayer.getCell(x, y);
				if (cell != null) {
					return new Vector2(x, y);
				}
			}
		}
		return Vector2.Zero;
	}
	
	private Npc createTestNpc(Vector2 position, String path, String asset) {
		return new Npc(InvokenGame.ACTOR_READER.readAsset(path), position.x, position.y, asset);
	}

	private void addActor(Agent actor) {
		entities.add(actor);
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0f / 255f, 0f / 255f, 0f / 255f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		// update player movement location
		if (player.isMoving()) {
			Vector3 world = camera.unproject(new Vector3(targetX, targetY, 0));
			player.moveTo(world.x, world.y);
		}
		
		// update UI menus
		dialogue.update(player);
		loot.update(player);
		
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
		
		// sort drawables by descending y
		Collections.sort(entities, new Comparator<Agent>() {
			@Override
			public int compare(Agent a1, Agent a2) {
				return Float.compare(a2.getPosition().y, a1.getPosition().y);
			}
		});

		// render the drawables
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
		
		// render the overlay layer
		renderer.render(overlays);
		
		// draw stats
		drawStats();
		
		// render the HUD
		stage.act(delta);
		stage.draw();

		batch.begin();
		font.draw(batch,
				"FPS: " + Gdx.graphics.getFramesPerSecond(),
				10, MENU_VIEWPORT_HEIGHT - 10);
		batch.end();
	}
	
	private void drawCentered(TextureRegion region, Vector2 position, Color color) {
		float w = 1 / 32f * region.getRegionWidth();
		float h = 1 / 32f * region.getRegionHeight();
		
		Batch batch = renderer.getSpriteBatch();
		batch.setColor(color);
		batch.begin();
		batch.draw(region, position.x - w / 2, position.y - h / 2 - 0.4f, w, h);
		batch.end();
		batch.setColor(Color.WHITE);
	}
	
	private void drawStats() {
		for (Agent actor : entities) {
			if (actor == player.getTarget() || actor == player) {
				drawStats(actor);
			}
		}
	}
	
	private void drawStats(Agent agent) {
		Vector2 position = agent.getPosition();
		float h = agent.getHeight() / 2;
		Vector3 screen = camera.project(new Vector3(position.x, position.y + h, 0));
		
		batch.begin();
		font.draw(batch, String.format("%.2f", agent.getHealth()), screen.x, screen.y);
		batch.end();
	}

	public List<Agent> getActors() {
		return entities;
	}
	
	public boolean isObstacle(int x, int y) {
		TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(collisionIndex);
		return layer.getCell(x, y) != null;
	}

	public Array<Rectangle> getTiles(int startX, int startY, int endX,
			int endY, Array<Rectangle> tiles) {
		TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(collisionIndex);
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
		case Keys.NUM_4:
			player.useAugmentation(3);
			return true;
		case Keys.I:
			inventoryMenu.toggle();
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
		// UI takes priority
		if (stage.touchDown(screenX, screenY, pointer, button)) {
			return true;
		}
		
		Vector3 world = camera.unproject(new Vector3(screenX, screenY, 0));
		for (Agent entity : entities) {
			if (entity.contains(world.x, world.y)) {
				if (entity == player) {
					playerClicked = true;
					return true;
				}
				return false;
			}
		}
		
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		// let UI handle first
		if (stage.touchUp(screenX, screenY, pointer, button)) {
			return true;
		}
		
		// always stop moving when no longer being dragged
		player.setMoving(false);
		
		// handle entity selection
		boolean selection = false;
		Vector3 world = camera.unproject(new Vector3(screenX, screenY, 0));
		for (Agent entity : entities) {
			if (entity.contains(world.x, world.y)) {
				if (player.getTarget() != entity) {
					// initial selection -> set target
					player.select(entity);
				} else {
					// already selected -> start interaction
					player.reselect(entity);
				}
				selection = true;
				break;
			}
		}
		
		if (!playerClicked && !selection) {
			// clicked on a non-interactive object, so deselect
			player.select(null);
		}
		playerClicked = false;
		
		return selection;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		if (playerClicked) {
			targetX = screenX;
			targetY = screenY;
			player.setMoving(true);
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