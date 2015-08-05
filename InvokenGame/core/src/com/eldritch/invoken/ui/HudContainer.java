package com.eldritch.invoken.ui;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.util.Settings;

public class HudContainer implements HudElement {
    private final Table container;
    private final List<HudElement> children = new ArrayList<>();
    
    public HudContainer(Skin skin) {
        container = new Table(skin);
        resize(Settings.MENU_VIEWPORT_WIDTH, Settings.MENU_VIEWPORT_HEIGHT);
        container.right().top();
    }
    
    public void addRow(HudElement element) {
        container.add(element.getContainer()).fillX().expandX();
        container.row();
        children.add(element);
    }

    @Override
    public Table getContainer() {
        return container;
    }

    @Override
    public void resize(int width, int height) {
        container.setHeight(height);
        container.setWidth(width);
        for (HudElement child : children) {
            child.resize(width, height);
        }
    }

    @Override
    public void update(float delta, Level level) {
        for (HudElement child : children) {
            child.update(delta, level);
        }
    }
}
