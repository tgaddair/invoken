package com.eldritch.invoken.ui;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.SplitPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.eldritch.invoken.actor.Inventory;
import com.eldritch.invoken.actor.items.Item;
import com.eldritch.invoken.actor.type.Player;
import com.eldritch.invoken.screens.AbstractScreen;
import com.eldritch.invoken.util.DefaultInputListener;

public class InventoryMenu {
	private final Window window;
	private final Table table;
	private final Skin skin;
	private final Player player;
	
	public InventoryMenu(Player player, Skin skin) {
		this.player = player;
		this.skin = skin;

	    table = new Table(skin);
		table.top();
		
		ScrollPane scroll = new ScrollPane(table, skin);
		ScrollPane playerView = new ScrollPane(new Image(player.getPortrait()));
		SplitPane splitPane = new SplitPane(playerView, scroll, false, skin, "default-horizontal");
		
		window = new Window("Inventory", skin);
		window.setHeight(AbstractScreen.MENU_VIEWPORT_HEIGHT - 100);
		window.setWidth(AbstractScreen.MENU_VIEWPORT_WIDTH - 100);
		window.setPosition(
              AbstractScreen.MENU_VIEWPORT_WIDTH / 2 - window.getWidth() / 2,
              AbstractScreen.MENU_VIEWPORT_HEIGHT / 2 - window.getHeight() / 2);
		window.center();
		
		TextButton closeButton = new TextButton("X", skin);
		closeButton.addListener(new DefaultInputListener() {
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                show(false);
            }
        });
		
        window.getButtonTable().add(closeButton).height(window.getPadTop());
        window.row().fill().expandX();
        window.add(splitPane).expand().fill();
        window.setVisible(false);
	}
	
	public Window getTable() {
		return window;
	}
	
	public void toggle() {
		show(!window.isVisible());
	}
	
	public void show(boolean visible) {
	    window.setVisible(visible);
		if (visible) {
			refresh();
		}
	}
	
	public void refresh() {
		table.clear();
		for (Inventory.ItemState item : player.getInfo().getInventory().getItems()) {
			addItemButton(item);
		}
	}
	
	private void addItemButton(Inventory.ItemState itemState) {
		final Item item = itemState.getItem();
		final TextButton itemButton = new TextButton(getText(item, itemState.getCount()), skin);
		itemButton.addListener(new DefaultInputListener() {
			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
			    Inventory inventory = player.getInfo().getInventory();
			    if (item.isEquipped(inventory)) {
			        inventory.unequip(item);
			    } else {
			        inventory.equip(item);
			    }
			}
		});
		table.row();
		table.add(itemButton).expandX().fillX().padLeft(5).padRight(5).padBottom(5).padTop(5);
	}
	
	private String getText(Item item, int count) {
		return String.format("%s (%d)", item.getName(), count);
	}
}
