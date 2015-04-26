package com.eldritch.invoken.screens;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.location.Location;
import com.eldritch.invoken.ui.Toaster;
import com.eldritch.invoken.ui.Toaster.Message;
import com.eldritch.invoken.util.MusicManager;

/**
 * Shows a splash image and moves on to the next screen.
 */
public class CreditsScreen extends AbstractScreen implements InputProcessor {
    private static final float RUNTIME_SECS = 240f;  // four minutes
    private final Location location;
    private final OrthographicCamera camera;
    private final Toaster toaster;

    public CreditsScreen(InvokenGame game, Location location, OrthographicCamera camera) {
        super(game);
        this.location = location;
        this.camera = camera;
        toaster = new Toaster(getSkin());
    }

    @Override
    public void show() {
        super.show();
        stage.addActor(toaster.getContainer());
        
        List<String> lines = parseCredits();
        toaster.setDisplayTime(getDisplayTime(lines));

        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            if (line.trim().isEmpty()) {
                toaster.add(getMessage(sb.toString()));
                sb = new StringBuilder();
            } else {
                sb.append(line).append("\n");
            }
        }
        toaster.add(getMessage(sb.toString()));
        
        InvokenGame.MUSIC_MANAGER.play(MusicManager.CREDITS);
        Gdx.input.setInputProcessor(this);
    }
    
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f / 255f, 0f / 255f, 0f / 255f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        location.render(delta, camera, null, false);
        toaster.update(delta);
        stage.act(delta);
        stage.draw();
    }
    
    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        toaster.resize(width, height);
    }

    private Message getMessage(String text) {
        return new Message(text);
    }
    
    private float getDisplayTime(List<String> lines) {
        int gaps = 0;
        for (String line : lines) {
            if (line.trim().isEmpty()) {
                gaps++;
            }
        }
        
        int messageCount = gaps + 1;
        float totalSeconds = RUNTIME_SECS - toaster.getFadeTime() * 2 * messageCount;
        return totalSeconds / messageCount;
    }

    private List<String> parseCredits() {
        List<String> lines = new ArrayList<>();
        FileHandle handle = Gdx.files.internal("text/credits.txt");
        try (InputStream is = handle.read()) {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                lines.add(line);
            }
            br.close();
        } catch (IOException ex) {
            InvokenGame.error("Failed reading " + handle.name(), ex);
        }
        return lines;
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        game.setScreen(new MenuScreen(game, location, camera));
        return true;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        game.setScreen(new MenuScreen(game, location, camera));
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
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
}