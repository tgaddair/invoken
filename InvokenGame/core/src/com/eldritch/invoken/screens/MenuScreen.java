package com.eldritch.invoken.screens;

import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.async.AsyncExecutor;
import com.badlogic.gdx.utils.async.AsyncResult;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.Profession;
import com.eldritch.invoken.actor.type.Player;
import com.eldritch.invoken.location.Location;
import com.eldritch.invoken.location.proc.LocationGenerator;
import com.eldritch.invoken.proto.Actors.PlayerActor;
import com.eldritch.invoken.proto.Locations;
import com.eldritch.invoken.util.DefaultInputListener;
import com.eldritch.invoken.util.GameTransition;
import com.eldritch.invoken.util.MusicManager;
import com.eldritch.invoken.util.SoundManager;
import com.google.common.base.Optional;

public class MenuScreen extends AbstractScreen {
    private final GameTransition gameState = new GameTransition() {
        @Override
        public void transition(String locationName, Optional<String> encounterName,
                PlayerActor state) {
            // do nothing
        }
    };
    private OrthographicCamera camera;
    private Location location;

    private final AsyncExecutor executor = new AsyncExecutor(1);
    private AsyncResult<Location> locationFuture;

    public MenuScreen(InvokenGame game) {
        super(game);
    }
    
    public MenuScreen(InvokenGame game, Location location, OrthographicCamera camera) {
        super(game);
        this.location = location;
        this.camera = camera;
    }

    @Override
    public void show() {
        super.show();

        // retrieve the default table actor
        Table table = super.getTable();
        table.add(new Image(GameScreen.getTexture("sprite/logo.png"))).spaceBottom(150);
        table.row();

        // register the button "start game"
        TextButton startGameButton = new TextButton("New Game", getSkin());
        startGameButton.addListener(new DefaultInputListener() {
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                // game.getSoundManager().play(TyrianSound.CLICK);
                game.setScreen(new CharacterCreationScreen(game, location, camera));
            }
        });
        table.add(startGameButton).size(300, 60).uniform().spaceBottom(10);
        table.row();

        // register the button "start game"
        TextButton tutorialButton = new TextButton("Tutorial", getSkin());
        tutorialButton.addListener(new DefaultInputListener() {
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                // game.getSoundManager().play(TyrianSound.CLICK);
                game.setScreen(new GameScreen(game, Profession.getDefault(), "Tutorial"));
            }
        });
        table.add(tutorialButton).size(300, 60).uniform().spaceBottom(10);
        table.row();

        // register the button "options"
        TextButton optionsButton = new TextButton("Credits", getSkin());
        optionsButton.addListener(new DefaultInputListener() {
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                // game.getSoundManager().play(TyrianSound.CLICK);
                game.setScreen(new CreditsScreen(game, location, camera));
            }
        });
        table.add(optionsButton).size(300, 60).uniform().spaceBottom(10);
        table.row();

        // setup background world
        // location = null;
        // locationFuture = executor.submit(new AsyncTask<Location>() {
        // @Override
        // public Location call() throws Exception {
        // return bgWorldSetup();
        // }
        // });
        
        if (location == null) {
            location = bgWorldSetup();
        }

        // play title music
        InvokenGame.MUSIC_MANAGER.play(MusicManager.MAIN);

        SoundManager sounds = InvokenGame.SOUND_MANAGER;
        sounds.setCamera(camera);
        sounds.setEnabled(false);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f / 255f, 0f / 255f, 0f / 255f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // if (location == null) {
        // if (locationFuture.isDone()) {
        // try {
        // location = locationFuture.get();
        // location.resize(getWidth(), getHeight());
        // } catch (Exception e) {
        // throw new RuntimeException("Failed to background load location", e);
        // }
        // }
        // } else {
        // location.render(delta, camera, null, false);
        // }
        location.render(delta, camera, null, false);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        if (location != null) {
            location.resize(width, height);
        }
    }

    private Location bgWorldSetup() {
        // create an orthographic camera, shows us 10(w/h) x 10 units of the
        // world
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, (w / h) * 10, 10);
        camera.zoom = 2f;
        camera.update();

        Random rand = new Random();
        Locations.Location data = InvokenGame.LOCATION_READER.readAsset("WelcomeCenter");
        LocationGenerator generator = new LocationGenerator(gameState, data.getBiome(),
                rand.nextLong());
        Location location = generator.generate(data);
        Player player = location.createDummyPlayer();

        // init camera position
        Vector2 position = player.getCamera().getPosition();
        camera.position.x = location.scale(position.x, camera.zoom);
        camera.position.y = location.scale(position.y, camera.zoom);
        location.setCamera(camera);
        return location;
    }
}