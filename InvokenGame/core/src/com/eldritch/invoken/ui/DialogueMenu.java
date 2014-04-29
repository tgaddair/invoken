package com.eldritch.invoken.ui;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.DialogueManager;
import com.eldritch.invoken.actor.Npc;
import com.eldritch.invoken.actor.Player;
import com.eldritch.invoken.screens.AbstractScreen;
import com.eldritch.invoken.util.DefaultInputListener;
import com.eldritch.scifirpg.proto.Actors.DialogueTree.Choice;
import com.eldritch.scifirpg.proto.Actors.DialogueTree.Response;

public class DialogueMenu {
	private final Table container;
	private final Table table;
	private final Skin skin;
	private boolean active = false;
	
	public DialogueMenu(Skin skin) {
	    container = new Table(skin);
	    container.setHeight(AbstractScreen.MENU_VIEWPORT_HEIGHT / 2);
	    container.setWidth(AbstractScreen.MENU_VIEWPORT_WIDTH - 25);
		container.bottom();

	    table = new Table(skin);
		table.bottom();
		
		this.skin = skin;
		
		// TODO translucent background
//		ScrollPane scroll = new ScrollPane(table, skin);
		ScrollPane scroll = new ScrollPane(table);
		container.add(scroll).expand().fill();
		container.setVisible(false);
	}
	
	public void update(Player player) {
		if (player.inDialogue()) {
			if (!active) {
				container.setVisible(true);
				active = true;
				setup(player.getDialogue());
			}
		} else {
			endDialogue();
		}
	}
	
	private void setup(DialogueManager dialogue) {
		setup(dialogue.getDialoguer(), dialogue.getGreeting());
	}
	
	private void setup(Npc npc, Response response) {
		if (response != null) {
			InvokenGame.log("Dialogue: " + response.getText());
			
			// remove old content
			table.clear();
			
			// add new content
			addLabel(response.getText());
			for (final Choice c : npc.getChoicesFor(response)) {
				addChoiceButton(c, npc);
			}
		} else {
			// end of conversation
			endDialogue();
		}
	}
	
	public Table getTable() {
		return container;
	}
	
	private void endDialogue() {
		container.setVisible(false);
		active = false;
		// TODO: end dialogue with player
	}
	
	private void addLabel(String text) {
		Label label = new Label(text, skin);
		label.setWrap(true);
		label.setWidth(100);
		table.add(label).width(
				AbstractScreen.MENU_VIEWPORT_WIDTH - 50).padLeft(25).padRight(25).padBottom(10);
	}
	
	private void addChoiceButton(final Choice c, final Npc npc) {
		TextButton choice = new TextButton(c.getText(), skin);
		choice.addListener(new DefaultInputListener() {
			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				setup(npc, npc.getResponseFor(c));
			}
		});
		table.row();
		table.add(choice).left().padLeft(25).padRight(25).padBottom(10);
	}
}
