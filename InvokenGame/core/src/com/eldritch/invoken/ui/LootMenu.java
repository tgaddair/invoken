package com.eldritch.invoken.ui;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.eldritch.invoken.actor.AgentInfo.ItemState;
import com.eldritch.invoken.actor.Npc;
import com.eldritch.invoken.actor.Player;
import com.eldritch.invoken.screens.AbstractScreen;
import com.eldritch.invoken.util.DefaultInputListener;
import com.eldritch.scifirpg.proto.Items.Item;

public class LootMenu {
	private final Table container;
	private final Table table;
	private final Skin skin;
	private boolean active = false;
	
	public LootMenu(Skin skin) {
		this.skin = skin;
	    container = new Table(skin);
	    container.setHeight(AbstractScreen.MENU_VIEWPORT_HEIGHT - 100);
	    container.setWidth(AbstractScreen.MENU_VIEWPORT_WIDTH / 3);
		container.center();

	    table = new Table(skin);
		table.center();
		
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
	
	public void setup(Player player, Npc npc) {
		table.clear();
		for (ItemState item : npc.getInfo().getInventoryItems()) {
			addItemButton(item, player, npc);
		}
	}
	
	private void addItemButton(ItemState itemState, final Player player, final Npc npc) {
		final Item item = itemState.getItem();
		final TextButton itemButton = new TextButton(getText(item, itemState.getCount()), skin);
		itemButton.addListener(new DefaultInputListener() {
			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				npc.getInfo().removeItem(item);
				player.getInfo().addItem(item);
				
				// update button with new count
				int count = npc.getInfo().getItemCount(item);
				if (count > 0) {
					itemButton.setText(getText(item, count));
				} else {
					itemButton.setVisible(false);
				}
			}
		});
		table.row();
		table.add(itemButton).fillX().padLeft(25).padRight(25).padBottom(10);
	}
	
	private String getText(Item item, int count) {
		return String.format("%s (%d)", item.getName(), count);
	}
	
	private void exitMenu() {
		container.setVisible(false);
		active = false;
	}
}
