package com.eldritch.invoken.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.Profession.Ghost;
import com.eldritch.invoken.actor.items.Item;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Player;
import com.eldritch.invoken.encounter.Activator;
import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.encounter.proc.LocationGenerator;
import com.eldritch.invoken.ui.ActionBar;
import com.eldritch.invoken.ui.DialogueMenu;
import com.eldritch.invoken.ui.InventoryMenu;
import com.eldritch.invoken.ui.LootMenu;

public class GameScreen extends AbstractScreen implements InputProcessor {
    public static final float MIN_ZOOM = 1.0f;
    public static final float MAX_ZOOM = 10.0f; // for debug purposes; 2 or 1.5 is more reasonable
    
	public static final AssetManager textureManager = new AssetManager();
	
	private final DialogueMenu dialogue;
	private final LootMenu loot;
	private ActionBar actionBar;
	private InventoryMenu inventoryMenu;

	private Player player;
	private Location location;

	private TextureRegion selector;
	private OrthographicCamera camera;
	private BitmapFont font;
	private SpriteBatch batch;
	
	private boolean tacticalPause = false;
	
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

	@Override
	public void show() {
		super.show();

		// create an orthographic camera, shows us 10(w/h) x 10 units of the
		// world
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();

		camera = new OrthographicCamera();
		camera.setToOrtho(false, (w / h) * 10, 10);
		camera.zoom = 1.25f;
		camera.update();

		font = new BitmapFont();
		batch = new SpriteBatch();
		
		// load the selector
		selector = new TextureRegion(new Texture("sprite/selection.png"));
		
		// create the Player we want to move around the world
		player = new Player(new Ghost(), 25, 0, 0,
				"sprite/characters/light-blue-hair.png");
//		player.addFaction(playerFaction, 9, 0);
		
		Item outfit = Item.fromProto(InvokenGame.ITEM_READER.readAsset("IcarianOperativeExosuit"));
		player.getInfo().getInventory().addItem(outfit);
		player.getInfo().getInventory().equip(outfit);
		
		Item weapon = Item.fromProto(InvokenGame.ITEM_READER.readAsset("AssaultRifle"));
        player.getInfo().getInventory().addItem(weapon);
        player.getInfo().getInventory().equip(weapon);
		
//		location = new Location(
//		        InvokenGame.LOCATION_READER.readAsset("NostorraPlaza"), player);
		
		LocationGenerator generator = new LocationGenerator();
		location = generator.generate(
		        InvokenGame.LOCATION_READER.readAsset("NostorraUnderworks"), player);
		
		// create player menus
		actionBar = new ActionBar(player);
		inventoryMenu = new InventoryMenu(player, getSkin());
		stage.addActor(actionBar.getTable());
		stage.addActor(inventoryMenu.getTable());

		Gdx.input.setInputProcessor(this);
		Gdx.app.log(InvokenGame.LOG, "start");
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
        actionBar.update();
		dialogue.update(player);
		loot.update(player);
		
		// render the location
		location.render(delta, camera, selector, tacticalPause);
		
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
	
	private void drawStats() {
		for (Agent actor : location.getActors()) {
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

	@Override
	public boolean keyDown(int keycode) {
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		switch (keycode) {
		case Keys.NUM_1:
		    player.getInfo().getAugmentations().toggleActiveAugmentation(0);
			return true;
		case Keys.NUM_2:
		    player.getInfo().getAugmentations().toggleActiveAugmentation(1);
			return true;
		case Keys.NUM_3:
		    player.getInfo().getAugmentations().toggleActiveAugmentation(2);
			return true;
		case Keys.NUM_4:
		    player.getInfo().getAugmentations().toggleActiveAugmentation(3);
			return true;
		case Keys.I:
			inventoryMenu.toggle();
			return true;
		case Keys.SPACE:
		    tacticalPause = !tacticalPause;
		    return true;
		case Keys.BACKSPACE:
		    if (tacticalPause) {
		        player.removeAction();
		        return true;
		    }
		    return false;
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
		for (Agent entity : location.getActors()) {
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
		for (Agent entity : location.getActors()) {
			if (entity.contains(world.x, world.y)) {
			    if (player.getInfo().getAugmentations().hasActiveAugmentation()) {
			        if (player.select(entity, location)) {
			            player.getInfo().getAugmentations().useActiveAugmentation(tacticalPause);
			        }
                } else if (player.getTarget() != entity) {
					// initial selection -> set target
					player.select(entity, location);
				} else if (!tacticalPause) {
					// already selected -> start interaction
					player.reselect(entity);
				}
				selection = true;
				break;
			}
		}
		
		// handle activators
		for (Activator activator : location.getActivators()) {
            if (activator.click(world.x, world.y)) {
                selection = true;
                break;
            }
        }
		
		if (!playerClicked && !selection) {
			// clicked on a non-interactive object, so deselect
			player.select(null, location);
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
	    float zoom = camera.zoom + amount * 0.1f;
	    zoom = Math.min(Math.max(zoom, MIN_ZOOM), MAX_ZOOM);
	    camera.zoom = zoom;
		return true;
	}
	
	public static TextureRegion[][] getRegions(String assetName, int w, int h) {
		return TextureRegion.split(getTexture(assetName), w, h);
	}
	
	public static Texture getTexture(String assetName) {
	    if (!textureManager.isLoaded(assetName, Texture.class)) {
            textureManager.load(assetName, Texture.class);
            textureManager.finishLoading();
        }
	    return textureManager.get(assetName, Texture.class);
	}
}