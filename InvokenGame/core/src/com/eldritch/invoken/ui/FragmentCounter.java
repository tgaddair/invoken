package com.eldritch.invoken.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.eldritch.invoken.actor.items.Fragment;
import com.eldritch.invoken.actor.type.Player;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.util.Settings;
import com.eldritch.invoken.util.Utils;

public class FragmentCounter implements HudElement {
    private static final float DELAY = 0.025f;

    private final Table container;
    private final Label label;

    private int target = 0;
    private int current = 0;
    private float elapsed = 0;

    public FragmentCounter(Player player, Skin skin) {
        container = new Table(skin);
        resize(Settings.MENU_VIEWPORT_WIDTH, Settings.MENU_VIEWPORT_HEIGHT);
        container.right().top();

        LabelStyle labelStyle = skin.get("toast", LabelStyle.class);
        label = new Label("0", labelStyle);
        label.setAlignment(Align.center);
        label.setFontScale(1f);
        container.add(label).padRight(10f);
        container.addListener(Utils.createTooltip(Fragment.getInstance().getTooltipFor(player),
                skin));
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
                
                String text = String.valueOf(current);
                if (level.getPlayer().getInfo().canLevel()) {
                    text = "[CYAN]" + text;
                }
                label.setText(text);
                elapsed = 0;
            }
        }
    }
}
