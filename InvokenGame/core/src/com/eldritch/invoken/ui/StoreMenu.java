package com.eldritch.invoken.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
import com.eldritch.invoken.actor.items.Fragment;
import com.eldritch.invoken.actor.items.Item;
import com.eldritch.invoken.actor.type.Player;
import com.eldritch.invoken.actor.util.Lootable;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.state.Inventory.ItemState;
import com.eldritch.invoken.util.DefaultInputListener;
import com.eldritch.invoken.util.Settings;
import com.eldritch.invoken.util.SoundManager.SoundEffect;
import com.eldritch.invoken.util.Utils;

public class StoreMenu implements HudElement {
    private final Map<TextButton, Item> buttons = new HashMap<>();
    private final Table container;
    private final Table table;
    private final Skin skin;
    private final Player player;
    private boolean active = false;

    public StoreMenu(Player player, Skin skin) {
        this.player = player;
        this.skin = skin;
        container = new Table(skin);
        resize(Settings.MENU_VIEWPORT_WIDTH, Settings.MENU_VIEWPORT_HEIGHT);
        container.center();

        table = new Table(skin);
        table.top();
        
        Table mainTable = new Table(skin);
        mainTable.top();

        Label title = createLabel("VENDING MACHINE");
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
        if (player.isBartering()) {
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
        buttons.clear();
        table.clear();
        
        add(createLabel("[CYAN]Name"), table);
        add(createLabel("[CYAN]Cost"), table);
        add(createLabel("[CYAN]Count"), table);
        
        for (ItemState item : player.getBartered().getInventory().getItems()) {
            addItemButton(item, player, player.getBartered());
        }
    }

    private String getStyle(Item item) {
        if (canPurchase(player, item)) {
            return "choice";
        }
        return "invalid";
    }

    private void addItemButton(ItemState itemState, final Player player, final Lootable bartered) {
        final Item item = itemState.getItem();
        final Tooltip tooltip = Utils.createTooltip(item.getTooltipFor(player), skin);

        String styleName = getStyle(item);
        TextButtonStyle bStyle = skin.get(styleName, TextButtonStyle.class);
        final TextButton itemButton = new TextButton(item.getName(player), bStyle);

        final Label costLabel = createLabel(String.valueOf(item.getValue()));
        final Label countLabel = createLabel(String.valueOf(itemState.getCount()));

        itemButton.addListener(new DefaultInputListener() {
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (canPurchase(player, item)) {
                    // transfer 1 quantity from the bartered to the player
                    bartered.getInventory().removeItem(item, 1);
                    player.getInfo().getInventory().addItem(item, 1);
                    player.getInventory().removeItem(Fragment.getInstance(), item.getValue());

                    // update button with new count and new style if we can no longer afford
                    int count = bartered.getInventory().getItemCount(item);
                    countLabel.setText(String.valueOf(count));
                    if (count > 0) {
                        itemButton.setStyle(skin.get(getStyle(item), TextButtonStyle.class));
                    } else {
                        itemButton.setVisible(false);
                    }

                    // update cost
                    for (Entry<TextButton, Item> entry : buttons.entrySet()) {
                        Item otherItem = entry.getValue();
                        if (entry.getKey() != itemButton) {
                            TextButton otherButton = entry.getKey();
                            otherButton.setStyle(skin.get(getStyle(otherItem),
                                    TextButtonStyle.class));
                        }
                    }

                    // player sound effect
                    InvokenGame.SOUND_MANAGER.play(SoundEffect.INVENTORY_ON, 2);
                }
            }
        });
        itemButton.addListener(tooltip);

        table.row();
        add(itemButton, table);
        add(costLabel, table);
        add(countLabel, table);

        buttons.put(itemButton, item);
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

    private boolean canPurchase(Player player, Item item) {
        return player.isIdentified(item) && canAfford(player, item);
    }

    private boolean canAfford(Player player, Item item) {
        int cost = item.getValue();
        int fragments = player.getInventory().getItemCount(Fragment.getInstance());
        return fragments >= cost;
    }

    private void exitMenu() {
        container.setVisible(false);
        active = false;
    }
}
