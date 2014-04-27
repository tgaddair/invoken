package com.eldritch.invoken.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.eldritch.invoken.actor.Player;
import com.eldritch.invoken.screens.AbstractScreen;

public class DialogueMenu {
	private final Table container;
	
	public DialogueMenu(Skin skin) {
	    container = new Table(skin);
	    container.setHeight(AbstractScreen.MENU_VIEWPORT_HEIGHT / 2);
	    container.setWidth(AbstractScreen.MENU_VIEWPORT_WIDTH - 25);
		container.bottom();

	    Table table = new Table(skin);
		table.bottom();
		
		// TODO translucent background
//		ScrollPane scroll = new ScrollPane(table, skin);
		ScrollPane scroll = new ScrollPane(table);
		
	    String text = "This is a test showing a really long line of dialogue that might be seen in the game when speaking to some random NPC you encounter.";
		Label label = new Label(text, skin);
		label.setWrap(true);
		label.setWidth(100);
		table.add(label).width(
				AbstractScreen.MENU_VIEWPORT_WIDTH - 50).padLeft(25).padRight(25).padBottom(10);
		
		TextButton choice1 = new TextButton("Yes, I agree.", skin);
		table.row();
		table.add(choice1).left().padLeft(25).padRight(25).padBottom(10);
		
		TextButton choice2 = new TextButton("No, I cannot do that.", skin);
		table.row();
		table.add(choice2).left().padLeft(25).padRight(25).padBottom(10);
		
		container.add(scroll).expand().fill();
		container.setVisible(false);
	}
	
	public void update(Player player) {
		if (player.inDialogue()) {
			container.setVisible(true);
		}
	}
	
	public Table getTable() {
		return container;
	}
}
