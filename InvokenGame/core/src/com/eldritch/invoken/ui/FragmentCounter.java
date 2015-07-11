package com.eldritch.invoken.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.eldritch.invoken.actor.items.Fragment;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.util.Settings;

public class FragmentCounter implements HudElement {
    private static final float DELAY = 0.025f;
    
    private final Table container;
    private final Label label;
    
    private int target = 0;
    private int current = 0;
    private float elapsed = 0;
    
    public FragmentCounter(Skin skin) {
        container = new Table(skin);
        resize(Settings.MENU_VIEWPORT_WIDTH, Settings.MENU_VIEWPORT_HEIGHT);
        container.right().top();
        
        LabelStyle labelStyle = skin.get("toast", LabelStyle.class);
        label = new Label("0", labelStyle);
        label.setAlignment(Align.center);
        label.setFontScale(1f);
        container.add(label).padRight(10f);
    }
    
    @Override
    public Table getContainer() {
        return container;
    }
    
    @Override
    public void resize(int width, int height) {
        container.setHeight(height);
        container.setWidth(width);
    }
    
    @Override
    public void update(float delta, Level level) {
        target = level.getPlayer().getInventory().getItemCount(Fragment.getInstance());
        if (current != target) {
            elapsed += delta;
            if (elapsed > DELAY) {
                int dx = (int) Math.signum(target - current);
                current += dx;
                label.setText(String.valueOf(current));
                elapsed = 0;
            }
        }
    }
}
