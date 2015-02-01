package com.eldritch.invoken.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.activators.Activator;
import com.eldritch.invoken.actor.PreparedAugmentations;
import com.eldritch.invoken.actor.Profession;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Npc;
import com.eldritch.invoken.actor.type.Player;
import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.encounter.proc.LocationGenerator;
import com.eldritch.invoken.ui.ActionBar;
import com.eldritch.invoken.ui.DialogueMenu;
import com.eldritch.invoken.ui.StatusBar;
import com.eldritch.invoken.ui.HealthBar;
import com.eldritch.invoken.ui.InventoryMenu;
import com.eldritch.invoken.ui.LootMenu;
import com.eldritch.invoken.ui.StatusBar.EnergyCalculator;
import com.eldritch.invoken.ui.StatusBar.HealthCalculator;
import com.eldritch.invoken.util.Settings;

public class GameScreen extends AbstractScreen implements InputProcessor {
    public static final AssetManager textureManager = new AssetManager();
    
    public static boolean SCREEN_GRAB = false;
    
	private final DialogueMenu dialogue;
	private final LootMenu loot;
	private final Profession profession;  // TODO: this will become a proto containing play info
	
	private ActionBar actionBar;
	private InventoryMenu inventoryMenu;
	
	private Table statusTable;
	private StatusBar playerHealth;
	private StatusBar energyBar;
	
	private HealthBar selectedHealth;

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

	public GameScreen(InvokenGame game, Profession profession) {
		super(game);
		dialogue = new DialogueMenu(getSkin());
		loot = new LootMenu(getSkin());
		this.profession = profession;
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
		camera.zoom = 1.15f;
		camera.update();
		
		font = new BitmapFont();
		batch = new SpriteBatch();
		
		// load the selector
		selector = new TextureRegion(new Texture("sprite/selection.png"));
		
//		location = new Location(
//		        InvokenGame.LOCATION_READER.readAsset("NostorraPlaza"), player);
		
        com.eldritch.invoken.proto.Locations.Location data = 
//        		InvokenGame.LOCATION_READER.readAsset("IcarianEmbassy");
        		InvokenGame.LOCATION_READER.readAsset("DebugPlayground");
		LocationGenerator generator = new LocationGenerator(data.getBiome());
		location = generator.generate(data);
		player = location.createPlayer(profession);
		
		// create player menus
		actionBar = new ActionBar(player);
		energyBar = new StatusBar(player, new EnergyCalculator(), getSkin());
		inventoryMenu = new InventoryMenu(player, getSkin());
		playerHealth = new StatusBar(player, new HealthCalculator(), getSkin());
		selectedHealth = new HealthBar(getSkin());
		
		statusTable = new Table(getSkin());
		statusTable.setHeight(Settings.MENU_VIEWPORT_HEIGHT / 2);
		statusTable.setWidth(Settings.MENU_VIEWPORT_WIDTH);
		statusTable.bottom();
		statusTable.left();
		
		statusTable.add(playerHealth).expand().bottom().left();
		statusTable.add(energyBar).expand().bottom().right();
		
		stage.addActor(actionBar.getTable());
		stage.addActor(statusTable);
        stage.addActor(dialogue.getTable());
		stage.addActor(inventoryMenu.getTable());
        stage.addActor(loot.getTable());

		Gdx.input.setInputProcessor(this);
		Gdx.app.log(InvokenGame.LOG, "start");
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0f / 255f, 0f / 255f, 0f / 255f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		// update player movement location
        if (player.isMoving() && !player.hasFixedTarget()) {
            Vector3 world = camera.unproject(new Vector3(targetX, targetY, 0));
            player.moveTo(world.x, world.y);
        }
		
		// update UI menus
        actionBar.update();
        energyBar.update();
        playerHealth.update();
        selectedHealth.update(player, player.getTarget(), camera);
		dialogue.update(player, camera);
		loot.update(player);
		
		// update mouse position
		Vector3 world = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
        location.setFocusPoint(world.x, world.y);
		
		// render the location
		location.render(delta, camera, selector, tacticalPause);
		
		// draw health bars
		batch.begin();
		selectedHealth.draw(batch);
		dialogue.draw(batch);
		batch.end();
		
		// draw stats
		drawStats();
		
		// render the HUD
		stage.act(delta);
		stage.draw();

		drawFps();
		
		// reset
		SCREEN_GRAB = false;
	}
	
	@Override
    public void resize(int width, int height) {
	    super.resize(width, height);
	    actionBar.resize(width, height);
	    statusTable.setSize(width, height / 2);
        loot.resize(width, height);
        location.resize(width, height);
    }
	
	private void drawFps() {
	    batch.begin();
        font.draw(batch,
                "FPS: " + Gdx.graphics.getFramesPerSecond(),
                10, getHeight() - 10);
        batch.end();
	}
	
	private void drawStats() {
//		for (Agent actor : location.getActors()) {
//			if (actor == player.getTarget() || actor == player) {
//				drawStats(actor);
//			}
//		}
		
		if (player.getTarget() != null) {
			if (player.getTarget() instanceof Npc) {
				Npc npc = (Npc) player.getTarget();
				State<Npc> state = npc.getStateMachine().getCurrentState();
				float freezing = npc.getFreezing();
				boolean agitated = npc.isAgitated();
				int enemies = npc.getEnemyCount();
				
				int i = 0;
				batch.begin();
		        font.draw(batch, "Graph: " + Settings.DRAW_GRAPH, 10, getHeight() - (30 + 20 * i++));
		        font.draw(batch, "State: " + state, 10, getHeight() - (30 + 20 * i++));
		        font.draw(batch, String.format("Freezing: %.2f", freezing), 10, getHeight() - (30 + 20 * i++));
		        font.draw(batch, String.format("Agitated: %s", agitated), 10, getHeight() - (30 + 20 * i++));
		        font.draw(batch, String.format("Enemies: %d", enemies), 10, getHeight() - (30 + 20 * i++));
		        batch.end();
			}
		}
	}
	
	private void drawStats(Agent agent) {
		Vector2 position = agent.getRenderPosition();
		float h = agent.getHeight() / 2;
		Vector3 screen = camera.project(new Vector3(position.x, position.y + h, 0));
		
		batch.begin();
		font.draw(batch, String.format("%.2f", agent.getHealth()), screen.x, screen.y);
		batch.end();
	}

	@Override
	public boolean keyDown(int keycode) {
	    switch (keycode) {
	        case Keys.SHIFT_LEFT:
	            player.holdPosition(true);
	            return true;
	    }
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
		case Keys.F:
		    player.toggleLastAugmentation();
		    return true;
		case Keys.SPACE:
		    tacticalPause = !tacticalPause;
		    return true;
		case Keys.SHIFT_LEFT:
		    player.holdPosition(false);
		    return true;
		case Keys.BACKSPACE:
		    if (tacticalPause) {
		        player.removeAction();
		        return true;
		    }
		    return false;
		case Keys.MINUS:
		    Settings.lastDebugGraph();
		    return true;
		case Keys.EQUALS:
		    Settings.nextDebugGraph();
		    return true;
		case Keys.F1:
		    // print screen
		    SCREEN_GRAB = true;
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
		
		// handle entity selection
        boolean selection = false;
        Vector3 world = camera.unproject(new Vector3(screenX, screenY, 0));
        for (Agent entity : location.getActors()) {
            if (entity.contains(world.x, world.y) && player.canTarget(entity, location)) {
                selection = true;
                if (player.getInfo().getAugmentations().hasActiveAugmentation()
                        && player.select(entity, location)) {
                    player.getInfo().getAugmentations().useActiveAugmentation(
                            button, tacticalPause);
                } else if (player.getTarget() != entity) {
                    // initial selection -> set target
                    player.select(entity, location);
                } else if (!tacticalPause) {
                    // already selected -> start interaction
                    player.reselect(entity);
                } else {
                    selection = false;
                }
                
                if (selection) {
                    break;
                }
            }
        }
        
        // handle activators
        for (Activator activator : location.getActivators()) {
            if (activator.click(player, location, world.x, world.y)) {
                selection = true;
                break;
            }
        }
        
        if (!playerClicked && !selection) {
            // clicked on a non-interactive object, so deselect
            player.select(null, location);
        }
        playerClicked = false;
        
        if (!selection) {
            // click on arbitrary position
//          if (player.holdingPosition()) {
//              player.getInfo().getAugmentations().useActiveAugmentation(
//                      new Vector2(world.x, world.y), tacticalPause);
//          } else {
//              // move to destination
//                player.moveToFixedTarget(world.x, world.y);
//          }
            if (player.getInfo().getAugmentations().hasActiveAugmentation(button)) {
                player.getInfo().getAugmentations().useActiveAugmentation(
                        new Vector2(world.x, world.y), button, tacticalPause);
            } else {
                if (button == Input.Buttons.RIGHT) {
                    // turn on light
                    player.toggleLight();
                }
            }
            selection = true;
        }
        
        return selection;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		// let UI handle first
		if (stage.touchUp(screenX, screenY, pointer, button)) {
			return true;
		}
		
		// always stop moving when no longer being dragged
		player.setMoving(false);
		
		// handle the release of a sustained active augmentation, like the shield
		PreparedAugmentations prepared = player.getInfo().getAugmentations();
		if (prepared.hasActiveAugmentation(button)) {
		    prepared.release(button);
		}
		
		return false;
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
		return stage.mouseMoved(screenX, screenY);
	}

	@Override
	public boolean scrolled(int amount) {
	    float zoom = camera.zoom + amount * 0.1f;
	    zoom = Math.min(Math.max(zoom, Settings.MIN_ZOOM), Settings.MAX_ZOOM);
	    camera.zoom = zoom;
		return true;
	}
	
	public static TextureRegion[][] getRegions(String assetName, int w, int h) {
		return TextureRegion.split(getTexture(assetName), w, h);
	}
	
	public static TextureRegion[] getMergedRegion(String assetName, int w, int h) {
		TextureRegion[][] regions = getRegions(assetName, w, h);
		TextureRegion[] merged = new TextureRegion[regions.length * regions[0].length];
		for (int i = 0; i < regions.length; i++) {
			for (int j = 0; j < regions[i].length; j++) {
				merged[i * regions[i].length + j] = regions[i][j];
			}
		}
		return merged;
	}
	
	public static Texture getTexture(String assetName) {
	    if (!textureManager.isLoaded(assetName, Texture.class)) {
            textureManager.load(assetName, Texture.class);
            textureManager.finishLoading();
        }
	    return textureManager.get(assetName, Texture.class);
	}
}