package com.eldritch.invoken.screens;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.activators.Activator;
import com.eldritch.invoken.actor.AgentInfo;
import com.eldritch.invoken.actor.PreparedAugmentations;
import com.eldritch.invoken.actor.Profession;
import com.eldritch.invoken.actor.ai.NpcThreatMonitor.ThreatLevel;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Npc;
import com.eldritch.invoken.actor.type.Player;
import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.encounter.proc.LocationGenerator;
import com.eldritch.invoken.proto.Actors.PlayerActor;
import com.eldritch.invoken.proto.Locations;
import com.eldritch.invoken.ui.ActionBar;
import com.eldritch.invoken.ui.DialogueMenu;
import com.eldritch.invoken.ui.FragmentCounter;
import com.eldritch.invoken.ui.HealthBar;
import com.eldritch.invoken.ui.HudElement;
import com.eldritch.invoken.ui.InventoryMenu;
import com.eldritch.invoken.ui.LootMenu;
import com.eldritch.invoken.ui.Minimap;
import com.eldritch.invoken.ui.StatusBar;
import com.eldritch.invoken.ui.StatusBar.EnergyCalculator;
import com.eldritch.invoken.ui.StatusBar.HealthCalculator;
import com.eldritch.invoken.ui.Toaster;
import com.eldritch.invoken.ui.Toaster.Message;
import com.eldritch.invoken.util.Settings;
import com.google.common.base.Optional;

public class GameScreen extends AbstractScreen implements InputProcessor {
    public static final AssetManager textureManager = new AssetManager();
    public final static TextureAtlas ATLAS = new TextureAtlas(
            Gdx.files.internal("image-atlases/pages.atlas"));
    public final static TextureAtlas NORMAL_ATLAS = new TextureAtlas(
            Gdx.files.internal("image-atlases/normal/pages.atlas"));

    private static Toaster toaster;
    public static boolean SCREEN_GRAB = false;

    private final GameState gameState = new GameState();
    private final DialogueMenu dialogue;
    private final LootMenu loot;

    private final String playerName;
    private Profession profession = null; // TODO: this will become a proto containing play info

    private ActionBar actionBar;
    private InventoryMenu inventoryMenu;

    private Table statusTable;
    private StatusBar playerHealth;
    private StatusBar energyBar;
    private final List<HudElement> hud = new ArrayList<HudElement>();

    private HealthBar selectedHealth;

    private Player player;
    private Location location;

    private TextureRegion selector;
    private OrthographicCamera camera;
    private BitmapFont font;
    private SpriteBatch batch;

    private Minimap minimap;
    private boolean showMinimap = false;
    private boolean tacticalPause = false;

    // for clicks and drags
    boolean playerClicked = false;
    int targetX;
    int targetY;
    
    private static final float DODGE_WINDOW = 0.25f;
    private final Vector2 input = new Vector2();
    private boolean shiftDown = false;
    private float shiftTime = 0;

    private final Texture bg = getTexture("sprite/starfield.png");

    public GameScreen(InvokenGame game, String playerName) {
        this(game, playerName, null);
    }

    public GameScreen(InvokenGame game, Profession profession) {
        this(game, "Player", profession);
    }

    private GameScreen(InvokenGame game, String playerName, Profession profession) {
        super(game);
        dialogue = new DialogueMenu(getSkin());
        loot = new LootMenu(getSkin());
        this.playerName = playerName;
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

        if (profession == null) {
            // saved state
            PlayerActor state = load(playerName);

            // load the location
            Locations.Location data = InvokenGame.LOCATION_READER.readAsset(state.getLocation());
            LocationGenerator generator = new LocationGenerator(gameState, data.getBiome(),
                    state.getSeed());
            location = generator.generate(data);
            minimap = new Minimap(location.getMap(), location.getSeed());

            // load from disk
            player = location.createPlayer(state);
        } else {
            Random rand = new Random();
            Locations.Location data =
            // InvokenGame.LOCATION_READER.readAsset("DebugPlayground");
            // InvokenGame.LOCATION_READER.readAsset("CentralProcessing");
            // InvokenGame.LOCATION_READER.readAsset("WelcomeCenterLevel2");
            InvokenGame.LOCATION_READER.readAsset("WelcomeCenter");
            // InvokenGame.LOCATION_READER.readAsset("TestWorld1");
            // InvokenGame.LOCATION_READER.readAsset("DebugArena");
            LocationGenerator generator = new LocationGenerator(gameState, data.getBiome(),
                    rand.nextLong());
            location = generator.generate(data);
            minimap = new Minimap(location.getMap(), location.getSeed());

            // create a new player
            player = location.createPlayer(profession);

            // save in case they die before reaching the first save point
            save(player);
        }

        // init camera position
        Vector2 position = player.getCamera().getPosition();
        camera.position.x = location.scale(position.x, camera.zoom);
        camera.position.y = location.scale(position.y, camera.zoom);
        location.setCamera(camera);

        // create HUD elements
        actionBar = new ActionBar(player);
        energyBar = new StatusBar(player, new EnergyCalculator(), getSkin());
        inventoryMenu = new InventoryMenu(player, getSkin());
        playerHealth = new StatusBar(player, new HealthCalculator(), getSkin());
        selectedHealth = new HealthBar(getSkin());
        hud.add(new FragmentCounter(getSkin()));

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
        for (HudElement element : hud) {
            stage.addActor(element.getContainer());
        }

        // announce the new location
        toaster = new Toaster(getSkin());
        stage.addActor(toaster.getContainer());
        toast(location.getName());

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
        
        // handle UI actions
        if (shiftDown) {
            shiftTime += delta;
            if (shiftTime > DODGE_WINDOW) {
                player.sprint(true);
            }
        }

        // check that the player is still alive
        if (location.getPlayer().isCompletelyDead()) {
            // game over
            game.setScreen(new GameOverScreen(game, location.getPlayer().getInfo().getName()));
        }

        // update UI menus
        actionBar.update();
        energyBar.update();
        playerHealth.update();
        selectedHealth.update(player, player.getTarget(), camera);
        dialogue.update(delta, player, camera);
        loot.update(player);
        for (HudElement element : hud) {
            element.update(delta, location);
        }

        // draw our toast
        toaster.update(delta);

        // update mouse position
        Vector3 world = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
        location.setFocusPoint(world.x, world.y);

        // render the background
        // TODO: trim must use the 16x16 pixel patches for this to work
        // batch.begin();
        // batch.draw(bg, 0, 0, getWidth(), getHeight());
        // batch.end();

        // render the location
        location.render(delta, camera, selector, tacticalPause);

        // draw health bars
        selectedHealth.draw();
        batch.begin();
        dialogue.draw(batch);
        // draw minimap
        if (showMinimap) {
            minimap.render(location.getPlayer(), batch, getWidth(), getHeight());
        }
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
        toaster.resize(width, height);
        for (HudElement element : hud) {
            element.resize(width, height);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        location.dispose();
    }

    private void drawFps() {
        batch.begin();
        font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 10, getHeight() - 10);
        batch.end();
    }

    private void drawStats() {
        // for (Agent actor : location.getActors()) {
        // if (actor == player.getTarget() || actor == player) {
        // drawStats(actor);
        // }
        // }

        Agent target = player.getTarget();
        if (target != null) {
            float health = target.getInfo().getHealth();
            float energy = target.getInfo().getEnergy();
            int level = target.getInfo().getLevel();
            float freezing = target.getFreezing();
            int enemies = target.getThreat().getEnemyCount();
            float visibility = target.getVisibility();

            int i = 0;
            batch.begin();
            font.draw(batch, String.format("Health: %.0f", health), 10, getHeight()
                    - (30 + 20 * i++));
            font.draw(batch, String.format("Energy: %.0f", energy), 10, getHeight()
                    - (30 + 20 * i++));
            font.draw(batch, String.format("Level: %d", level), 10, getHeight() - (30 + 20 * i++));
            font.draw(batch, String.format("Freezing: %.2f", freezing), 10, getHeight()
                    - (30 + 20 * i++));
            font.draw(batch, String.format("Enemies: %d", enemies), 10, getHeight()
                    - (30 + 20 * i++));
            font.draw(batch, String.format("Visibility: %.2f", visibility), 10, getHeight()
                    - (30 + 20 * i++));

            AgentInfo info = target.getInfo();
            font.draw(batch, String.format("Warfare: %d", info.getWarfare()), 10, getHeight()
                    - (30 + 20 * i++));
            font.draw(batch, String.format("Automata: %d", info.getAutomata()), 10, getHeight()
                    - (30 + 20 * i++));
            font.draw(batch, String.format("Subterfuge: %d", info.getSubterfuge()), 10, getHeight()
                    - (30 + 20 * i++));
            font.draw(batch, String.format("Charisma: %d", info.getCharisma()), 10, getHeight()
                    - (30 + 20 * i++));

            if (target instanceof Npc) {
                Npc npc = (Npc) target;
                String task = npc.getLastTask();
                ThreatLevel threat = npc.getThreat().getLevel();
                boolean agitated = npc.isAgitated();
                boolean fatigued = npc.getFatigue().isExpended();
                float fatigue = npc.getFatigue().getValue();
                boolean intimidated = npc.getIntimidation().isExpended();
                float intimidation = npc.getIntimidation().getValue();
                String targetName = npc.hasTarget() ? npc.getTarget().getInfo().getName() : "None";

                font.draw(batch, "Graph: " + Settings.DRAW_GRAPH, 10, getHeight() - (30 + 20 * i++));
                font.draw(batch, "Task: " + task, 10, getHeight() - (30 + 20 * i++));
                font.draw(batch, "Threat: " + threat, 10, getHeight() - (30 + 20 * i++));
                font.draw(batch, String.format("Aiming: %s", npc.isAiming()), 10, getHeight()
                        - (30 + 20 * i++));
                font.draw(batch, String.format("Sighted: %s", npc.hasSights()), 10,
                        getHeight() - (30 + 20 * i++));
                // font.draw(batch, String.format("Fatigued: %s (%.2f)", fatigued, fatigue), 10,
                // getHeight() - (30 + 20 * i++));
                font.draw(batch,
                        String.format("Intimidated: %s (%.2f)", intimidated, intimidation), 10,
                        getHeight() - (30 + 20 * i++));
                font.draw(batch, String.format("Target: %s", targetName), 10, getHeight()
                        - (30 + 20 * i++));
            }

            batch.end();
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

    private void printPlayerStatus() {
        StringBuilder sb = new StringBuilder("Player:\n");
        sb.append(String.format("Health: %.2f\n", player.getInfo().getHealth()));
        sb.append(String.format("Energy: %.2f\n", player.getInfo().getEnergy()));
        sb.append(String.format("Level: %d\n", player.getInfo().getLevel()));
        System.out.println(sb.toString());
    }
    
    private Vector2 getInputDirection() {
        input.set(Vector2.Zero);
        if (Gdx.input.isKeyPressed(Keys.A)) {
            input.x += -1;
        }
        if (Gdx.input.isKeyPressed(Keys.D)) {
            input.x += 1;
        }
        if (Gdx.input.isKeyPressed(Keys.S)) {
            input.y += -1;
        }
        if (Gdx.input.isKeyPressed(Keys.W)) {
            input.y += 1;
        }
        return input.nor();
    }
    
    private void shiftDown(boolean down) {
        this.shiftDown = down;
        if (!down) {
            player.sprint(false);
            if (shiftTime <= DODGE_WINDOW) {
                player.dodge(getInputDirection());
            }
        }
        shiftTime = 0;
    }

    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            case Keys.SPACE:
            case Keys.SHIFT_LEFT:
//                player.holdPosition(true);
                shiftDown(true);
                return true;
            case Keys.TAB:
                // show minimap
                showMinimap = true;
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
            case Keys.TAB:
                showMinimap = false;
                return true;

                // debug
            case Keys.P:
                printPlayerStatus();
                return true;
            case Keys.SPACE:
//                tacticalPause = !tacticalPause;
//                return true;
            case Keys.SHIFT_LEFT:
//                player.holdPosition(false);
                shiftDown(false);
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
            case Keys.NUMPAD_0:
                loadLocation("WelcomeCenterLevel2", Optional.<String> absent(), location
                        .getPlayer().serialize());
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

        if (button == Input.Buttons.RIGHT) {
            // turn on light
            // player.toggleLight();
            player.setAiming(true);
            return true;
        }

        if (player.inForcedDialogue()) {
            // can't do anything
            return false;
        }

        Vector3 world = camera.unproject(new Vector3(screenX, screenY, 0));

        // when the player is aiming, the only option is to use the augmentation
        if (player.isAiming()) {
            if (player.getInfo().getAugmentations().hasActiveAugmentation(button)) {
                player.getInfo()
                        .getAugmentations()
                        .useActiveAugmentation(new Vector2(world.x, world.y), button, tacticalPause);
                return true;
            }
            return false;
        }

        // handle entity selection
        boolean selection = false;
        for (Agent entity : location.getActors()) {
            if (entity.contains(world.x, world.y)) { // && player.canTarget(entity, location)) {
                selection = true;
                if (player.getInfo().getAugmentations().hasActiveAugmentation(button)
                        && player.getInfo().getAugmentations().getActiveAugmentation(button)
                                .isValid(player) && player.select(entity, location)) {
                    player.getInfo().getAugmentations()
                            .useActiveAugmentation(button, tacticalPause);
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
                // allow multiple activators
                selection = true;
            }
        }

        if (!playerClicked && !selection) {
            // clicked on a non-interactive object, so deselect
            player.select(null, location);
        }
        playerClicked = false;

        if (!selection) {
            // click on arbitrary position
            // if (player.holdingPosition()) {
            // player.getInfo().getAugmentations().useActiveAugmentation(
            // new Vector2(world.x, world.y), tacticalPause);
            // } else {
            // // move to destination
            // player.moveToFixedTarget(world.x, world.y);
            // }
            if (player.getInfo().getAugmentations().hasActiveAugmentation(button)) {
                player.getInfo()
                        .getAugmentations()
                        .useActiveAugmentation(new Vector2(world.x, world.y), button, tacticalPause);
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

        if (button == Input.Buttons.RIGHT) {
            player.setAiming(false);
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

    public static void toast(String text) {
        toaster.add(new Message(text));
    }

    public static TextureRegion[][] getRegions(String assetName, int w, int h) {
        return TextureRegion.split(getTexture(assetName), w, h);
    }

    public static TextureRegion[] getMergedRegion(String assetName, int w, int h) {
        return getMergedRegion(getRegions(assetName, w, h));
    }

    public static TextureRegion[] getMergedRegion(TextureRegion[][] regions) {
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

    public PlayerActor load(String name) {
        FileHandle handle = Gdx.files.local("saves/" + name + ".dat");
        try {
            return PlayerActor.parseFrom(handle.readBytes());
        } catch (Exception ex) {
            InvokenGame.error("Failed reading " + handle.name(), ex);
            return null;
        }
    }

    private void loadLocation(String locationName, Optional<String> encounterName, PlayerActor state) {
        // dispose of the current location
        location.dispose();

        // load the location
        Locations.Location data = InvokenGame.LOCATION_READER.readAsset(locationName);
        LocationGenerator generator = new LocationGenerator(gameState, data.getBiome(),
                state.getSeed());
        location = generator.generate(data, encounterName);
        location.spawnPlayer(player);
        minimap = new Minimap(location.getMap(), location.getSeed());

        // resize
        location.resize(getWidth(), getHeight());

        // init camera position
        Vector2 position = player.getCamera().getPosition();
        camera.position.x = location.scale(position.x, camera.zoom);
        camera.position.y = location.scale(position.y, camera.zoom);
        location.setCamera(camera);

        // announce the new location
        toaster = new Toaster(getSkin());
        stage.addActor(toaster.getContainer());
        toast(location.getName());
    }

    public static void save(Location location) {
        save(location.getPlayer());
    }

    private static void save(Player player) {
        FileHandle handle = Gdx.files.local("saves/" + player.getInfo().getName() + ".dat");
        try {
            final boolean append = false;
            handle.writeBytes(player.serialize().toByteArray(), append);
        } catch (Exception ex) {
            InvokenGame.error("Failed writing " + handle.name(), ex);
        }
    }

    public class GameState {
        public void transition(String locationName, Optional<String> encounterName,
                PlayerActor state) {
            loadLocation(locationName, encounterName, state);
        }
    }
}