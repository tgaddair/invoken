package com.eldritch.invoken.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.eldritch.invoken.actor.Inventory;
import com.eldritch.invoken.actor.items.Item;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Player;
import com.eldritch.invoken.util.DefaultInputListener;
import com.eldritch.invoken.util.Settings;

public class LootMenu {
	private final Table container;
	private final Table table;
	private final Skin skin;
	private boolean active = false;
	
	public LootMenu(Skin skin) {
		this.skin = skin;
	    container = new Table(skin);
	    resize(Settings.MENU_VIEWPORT_WIDTH, Settings.MENU_VIEWPORT_HEIGHT);
		container.center();

	    table = new Table(skin);
		table.top();
		
		ScrollPane scroll = new ScrollPane(table, skin);
		container.add(scroll).expand().fill();
		container.setVisible(false);
	}
	
	public Table getTable() {
		return container;
	}
	
	public void update(Player player) {
		if (player.isLooting()) {
			if (!active) {
				container.setVisible(true);
				active = true;
				setup(player, player.getInteractor());
			}
		} else {
			exitMenu();
		}
	}
	
	public void resize(int width, int height) {
	    container.setHeight(height - 100);
        container.setWidth(width / 3);
        container.setPosition(
                width / 2 - container.getWidth() / 2,
                height / 2 - container.getHeight() / 2);
	}
	
	public void setup(Player player, Agent npc) {
		table.clear();
		for (Inventory.ItemState item : npc.getInfo().getInventory().getItems()) {
			addItemButton(item, player, npc);
		}
	}
	
	private void addItemButton(Inventory.ItemState itemState, final Player player, final Agent npc) {
		final Item item = itemState.getItem();
		TextButtonStyle buttonStyle = skin.get("choice", TextButtonStyle.class);
		final TextButton itemButton = new TextButton(getText(item, itemState.getCount()), buttonStyle);
		itemButton.addListener(new DefaultInputListener() {
			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
			    int count = button == Input.Buttons.RIGHT 
			            ? 1 : npc.getInfo().getInventory().getItemCount(item);
				npc.getInfo().getInventory().removeItem(item, count);
				player.getInfo().getInventory().addItem(item, count);
				
				// update button with new count
				count = npc.getInfo().getInventory().getItemCount(item);
				if (count > 0) {
					itemButton.setText(getText(item, count));
				} else {
					itemButton.setVisible(false);
				}
			}
		});
		table.row();
		table.add(itemButton).expandX().fillX().padLeft(5).padRight(5).padBottom(5).padTop(5);
	}
	
	private String getText(Item item, int count) {
		return String.format("%s (%d)", item.getName(), count);
	}
	
	private void exitMenu() {
		container.setVisible(false);
		active = false;
	}
}
