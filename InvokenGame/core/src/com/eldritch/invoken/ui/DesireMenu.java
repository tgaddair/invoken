package com.eldritch.invoken.ui;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.eldritch.invoken.actor.ai.planning.Desire;
import com.eldritch.invoken.actor.aug.Empathy;
import com.eldritch.invoken.actor.type.Npc;
import com.eldritch.invoken.actor.type.Player;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.ui.StatusBar.StatusCalculator;
import com.eldritch.invoken.util.Settings;

public class DesireMenu implements HudElement {
    private final Table container;
    private final Table table;
    private final Skin skin;
    private final Player player;
    
    private final List<StatusBar<Npc>> bars = new ArrayList<>();

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
    public void update(float delta, Level level) {
        if (canActivate()) {
            if (!active) {
                setActive(true);
                refresh();
            }
            
            for (StatusBar<Npc> bar : bars) {
                bar.update();
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
        bars.clear();
        
        Npc target = (Npc) player.getTarget();
        List<Desire> desires = target.getPlanner().getDesires();
        for (final Desire desire : desires) {
            StatusCalculator<Npc> status = new StatusCalculator<Npc>() {
                @Override
                public float getStatus(Npc npc) {
                    // current value
                    return 100f * desire.getValue();
                }

                @Override
                public float getBaseStatus(Npc npc) {
                    // max value
                    return 100f;
                }

                @Override
                public String getStyleName() {
                    return "default-vertical";
                }
            };
            
            StatusBar<Npc> bar = new StatusBar<>(target, status, skin);
            bars.add(bar);
            table.add(bar).space(50);
        }
    }

    private boolean canActivate() {
        return player.isToggled(Empathy.class) && player.hasTarget()
                && player.getTarget() instanceof Npc;
    }
}
