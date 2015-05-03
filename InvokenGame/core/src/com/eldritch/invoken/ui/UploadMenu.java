package com.eldritch.invoken.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.AgentInventory;
import com.eldritch.invoken.actor.Profession;
import com.eldritch.invoken.actor.items.Item;
import com.eldritch.invoken.actor.type.Player;
import com.eldritch.invoken.location.Location;
import com.eldritch.invoken.proto.Disciplines.Discipline;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.state.Inventory.ItemState;
import com.eldritch.invoken.util.DefaultInputListener;
import com.eldritch.invoken.util.Settings;
import com.eldritch.invoken.util.SoundManager.SoundEffect;

public class UploadMenu implements HudElement {
    private final Table container;
    private final Table table;
    private final Skin skin;
    private final Player player;

    private boolean active = false;

    public UploadMenu(Player player, Skin skin) {
        this.player = player;
        this.skin = skin;

        table = new Table(skin);
        table.top();

        container = new Table(skin);
        resize(Settings.MENU_VIEWPORT_WIDTH, Settings.MENU_VIEWPORT_HEIGHT);
        container.center();

        ScrollPane scroll = new ScrollPane(table, skin);
        container.add(scroll).expand().fill();
        container.setVisible(false);
    }

    @Override
    public Table getContainer() {
        return container;
    }

    @Override
    public void resize(int width, int height) {
        container.setHeight(height - 100);
        container.setWidth(width - 100);
        container.setPosition(width / 2 - container.getWidth() / 2,
                height / 2 - container.getHeight() / 2);
        container.center();
    }

    @Override
    public void update(float delta, Location location) {
        if (player.isUploading()) {
            if (!active) {
                setActive(true);
                refresh();
            }
        } else {
            setActive(false);
        }
    }

    private void setActive(boolean value) {
        if (active != value) {
            container.setVisible(value);
            active = value;
        }
    }

    private void refresh() {
        table.clear();
        
        Table topTable = new Table(skin);
        topTable.top();
        
        Label disciplineLabel = new Label("Disciplines", skin);
        topTable.add(disciplineLabel).left().expandX().fillX().space(10);
        topTable.row();

        // disciplines
        int i = 0;
        Table disciplinesTable = new Table(skin);
        for (Discipline d : Discipline.values()) {
            Table disciplineTable = createTable(d);
            disciplinesTable.add(disciplineTable).fillX().expandX();
            if (++i % 2 == 0) {
                disciplinesTable.row();
            }
        }
        topTable.add(disciplinesTable).expandX().fillX().space(10);
        
        table.add(topTable).expand().fill();
        table.row();

        // commit button
        table.bottom().right();
        TextButton start = createCommitButton();
        table.add(start).size(200, 50).right();
    }

    private Table createTable(Discipline d) {
        Table table = new Table(skin);

        // image
        Image image = new Image(Profession.getIcon(d));
        table.add(image).size(50, 50).space(10);

        // label
        Label label = new Label(d.name(), skin);
        label.setColor(Color.GRAY);
        table.add(label).size(150, 50).space(10);

        // current value
        String skillLevel = String.valueOf(player.getInfo().getSkillLevel(d));
        final TextButton button = new TextButton(skillLevel, skin);
        button.addListener(new DefaultInputListener() {
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int buttonNumber) {
                super.touchUp(event, x, y, pointer, buttonNumber);

                // increment
                int value = Integer.valueOf(button.getText().toString()) + 1;
                button.setText(String.valueOf(value));
            }
        });
        table.add(button).size(50, 50).space(10);

        return table;
    }

    private TextButton createCommitButton() {
        TextButton button = new TextButton("Commit", skin);
        button.addListener(new DefaultInputListener() {
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                GameScreen.toast("Saving...");
                GameScreen.save(player.getLocation());
            }
        });
        return button;
    }
}
