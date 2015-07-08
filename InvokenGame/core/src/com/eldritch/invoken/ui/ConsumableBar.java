package com.eldritch.invoken.ui;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.eldritch.invoken.actor.AgentInventory;
import com.eldritch.invoken.actor.items.Consumable;
import com.eldritch.invoken.actor.type.Player;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.util.DefaultInputListener;
import com.eldritch.invoken.util.Settings;

public class ConsumableBar implements HudElement {
    private final Map<Consumable, Label> labels = new HashMap<>();
    private final Map<Consumable, Image> images = new HashMap<>();
    private final AgentInventory inv;
    private final Table container;
    private final Skin skin;

    public ConsumableBar(Player player, Skin skin) {
        this.skin = skin;
        this.inv = player.getInventory();

        container = new Table();
        resize(Settings.MENU_VIEWPORT_WIDTH, Settings.MENU_VIEWPORT_HEIGHT);
        container.bottom().left();
        refresh();
    }

    @Override
    public void update(float delta, Level level) {
        for (Consumable consumable : inv.getConsumables()) {
            if (labels.containsKey(consumable)) {
                if (inv.hasItem(consumable)) {
                    images.get(consumable).setColor(Color.WHITE);
                    labels.get(consumable).setText(String.valueOf(inv.getItemCount(consumable)));
                } else {
                    images.get(consumable).setColor(Color.GRAY);
                    labels.get(consumable).setText("");
                }
            }
        }
    }

    public void resize(int width, int height) {
        container.setHeight(height / 2);
        container.setWidth(width);
    }

    @Override
    public Table getContainer() {
        return container;
    }

    public void onClear() {
        container.clear();
        labels.clear();
        images.clear();
        refresh();
    }

    private void refresh() {
        for (Consumable consumable : inv.getConsumables()) {
            if (consumable != null) {
                add(consumable);
            }
        }
    }

    private void add(final Consumable consumable) {
        LabelStyle labelStyle = skin.get("toast", LabelStyle.class);
        Label label = new Label("", labelStyle);
        label.setAlignment(Align.topRight, Align.left);
        label.setColor(Color.GREEN);
        labels.put(consumable, label);

        // Label keyLabel = new Label("Z", labelStyle);
        // keyLabel.setAlignment(Align.topLeft, Align.left);
        // keyLabel.setColor(Color.CYAN);

        Image image = new Image(consumable.getIcon());
        images.put(consumable, image);

        Stack stack = new Stack();
        stack.addActor(image);
        // stack.addActor(keyLabel);
        stack.addActor(label);
        stack.addListener(new DefaultInputListener() {
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                inv.equip(consumable);
            }
        });

        container.add(stack).padLeft(10).padRight(10).padBottom(10);
        container.row();
    }
}
