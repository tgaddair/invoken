package com.eldritch.invoken.ui;

import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.eldritch.invoken.actor.aug.Empathy;
import com.eldritch.invoken.actor.type.Player;
import com.eldritch.invoken.location.Location;
import com.eldritch.invoken.util.Settings;

public class DesireMenu implements HudElement {
    private final Table container;
    private final Table table;
    private final Skin skin;
    private final Player player;

    private boolean active = false;

    public DesireMenu(Player player, Skin skin) {
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
        if (player.isToggled(Empathy.class) && player.hasTarget()) {
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
        
    }
}
