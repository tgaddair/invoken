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
import com.eldritch.scifirpg.proto.Actors.DialogueTree.Choice;

public class LootMenu {
	private final Table container;
	private final Table table;
	private final Skin skin;
	private boolean active = false;
	
	public LootMenu(Skin skin) {
		this.skin = skin;
	    container = new Table(skin);
	    container.setHeight(AbstractScreen.MENU_VIEWPORT_HEIGHT - 100);
	    container.setWidth(AbstractScreen.MENU_VIEWPORT_WIDTH / 4);
		container.bottom();

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
				setup(player.getInteractor());
			}
		} else {
			exitMenu();
		}
	}
	
	public void setup(Npc npc) {
		for (ItemState item : npc.getInfo().getInventoryItems()) {
			addItemButton(item);
		}
	}
	
	private void addItemButton(final ItemState item) {
		String text = String.format("%s (%d)", item.getItem().getName(), item.getCount());
		TextButton choice = new TextButton(text, skin);
		choice.addListener(new DefaultInputListener() {
			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
			}
		});
		table.row();
		table.add(choice).left().padLeft(25).padRight(25).padBottom(10);
	}
	
	private void exitMenu() {
		container.setVisible(false);
		active = false;
	}
}
