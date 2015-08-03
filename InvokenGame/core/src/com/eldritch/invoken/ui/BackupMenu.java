package com.eldritch.invoken.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.utils.Align;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.AgentInfo;
import com.eldritch.invoken.actor.items.Core;
import com.eldritch.invoken.actor.items.Fragment;
import com.eldritch.invoken.actor.type.Player;
import com.eldritch.invoken.actor.util.Backup;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.util.DefaultInputListener;
import com.eldritch.invoken.util.Settings;
import com.eldritch.invoken.util.SoundManager.SoundEffect;
import com.eldritch.invoken.util.Utils;

public class BackupMenu implements HudElement {
    private final Table container;
    private final Table table;
    private final Skin skin;
    private final Player player;

    private boolean canPurchase = true;
    private boolean active = false;

    public BackupMenu(Player player, Skin skin) {
        this.player = player;
        this.skin = skin;
        container = new Table(skin);
        resize(Settings.MENU_VIEWPORT_WIDTH, Settings.MENU_VIEWPORT_HEIGHT);
        container.center();

        table = new Table(skin);
        table.top();

        Table mainTable = new Table(skin);
        mainTable.top();

        Label title = createLabel("IMAGING STATION");
        add(title, mainTable, 10);
        mainTable.row();
        mainTable.add(table).expand().fill();

        ScrollPane scroll = new ScrollPane(mainTable, skin);
        container.add(scroll).expand().fill();
        container.setVisible(false);
    }

    @Override
    public Table getContainer() {
        return container;
    }

    @Override
    public void update(float delta, Level level) {
        if (player.isToggled(Backup.class)) {
            if (!active) {
                container.setVisible(true);
                active = true;
                setup(player);
            }
        } else {
            exitMenu();
        }
    }

    public void resize(int width, int height) {
        container.setHeight(height - 100);
        container.setWidth(width / 3);
        container.setPosition(width / 2 - container.getWidth() / 2,
                height / 2 - container.getHeight() / 2);
    }

    public void setup(Player player) {
        canPurchase = true;
        table.clear();

        add(createLabel("[CYAN]Name"), table);
        add(createLabel("[CYAN]Cost"), table);
        addButton(player);
    }

    private void addButton(final Player player) {
        final Tooltip tooltip = Utils.createTooltip("", skin);

        final Label costLabel = createLabel(String.valueOf(getCost(player)));

        String styleName = canAfford(player) ? "choice" : "invalid";
        TextButtonStyle buttonStyle = skin.get(styleName, TextButtonStyle.class);
        final TextButton itemButton = new TextButton("Create Backup", buttonStyle);

        itemButton.addListener(new DefaultInputListener() {
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (canPurchase && canAfford(player)) {
                    // backup
                    player.createBackup();
                    player.getInventory().removeItem(Fragment.getInstance(), getCost(player));
                    canPurchase = false;

                    // update button
                    costLabel.setText("N/A");
                    itemButton.setStyle(skin.get("inactive", TextButtonStyle.class));
                }

                InvokenGame.SOUND_MANAGER.play(SoundEffect.INVENTORY_ON, 2);
            }
        });
        itemButton.addListener(tooltip);

        table.row();
        add(itemButton, table);
        add(costLabel, table);
    }

    private void add(Actor actor, Table table) {
        add(actor, table, 5);
    }

    private void add(Actor actor, Table table, float pad) {
        table.add(actor).center().expandX().fillX().padLeft(pad).padRight(pad).padBottom(pad)
                .padTop(pad);
    }

    private Label createLabel(String text) {
        Label label = new Label(text, skin.get("default-nobg", LabelStyle.class));
        label.setAlignment(Align.center);
        return label;
    }

    private boolean canAfford(Player player) {
        if (!player.getInventory().hasItem(Core.getInstance())) {
            return false;
        }
        
        int cost = getCost(player);
        int fragments = player.getInventory().getItemCount(Fragment.getInstance());
        return fragments >= cost;
    }

    private int getCost(Player player) {
        return AgentInfo.getFragmentRequirement(player.getInfo().getLevel() + 1);
    }

    private void exitMenu() {
        container.setVisible(false);
        active = false;
    }
}
