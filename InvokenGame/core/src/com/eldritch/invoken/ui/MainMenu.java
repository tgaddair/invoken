package com.eldritch.invoken.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.eldritch.invoken.actor.type.Player;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.util.DefaultInputListener;
import com.eldritch.invoken.util.Settings;

public class MainMenu implements HudElement {
    private final Table container;
    private final Table table;
    private final Skin skin;

    public MainMenu(final Player player, Skin skin) {
        this.skin = skin;

        table = new Table(skin);
        table.top();

        container = new Table(skin);
        resize(Settings.MENU_VIEWPORT_WIDTH, Settings.MENU_VIEWPORT_HEIGHT);
        container.center();

        // create the table
        Table table = new Table(skin);
        table.top();

        // add buttons
        TextButton continueButton = addButton("Continue", table);
        continueButton.addListener(new DefaultInputListener() {
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                show(false);
            }
        });
        
        TextButton exitButton = addButton("Save & Exit", table);
        exitButton.addListener(new DefaultInputListener() {
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                GameScreen.save(player.getLocation());
                Gdx.app.exit();
            }
        });

        // add scroll pane
        ScrollPane scroll = new ScrollPane(table, skin);
        container.add(scroll).expand().fill();
        container.setVisible(false);
    }

    private TextButton addButton(String text, Table table) {
        TextButton button = new TextButton(text, skin);
        table.add(button).size(300, 60).uniform().spaceBottom(10);
        table.row();
        return button;
    }

    @Override
    public Table getContainer() {
        return container;
    }

    @Override
    public void resize(int width, int height) {
        container.setHeight(height - 100);
        container.setWidth(width / 3);
        container.setPosition(width / 2 - container.getWidth() / 2,
                height / 2 - container.getHeight() / 2);
    }

    @Override
    public void update(float delta, Level level) {
    }

    public void toggle() {
        show(!container.isVisible());
    }

    private void show(boolean visible) {
        container.setVisible(visible);
    }
}
