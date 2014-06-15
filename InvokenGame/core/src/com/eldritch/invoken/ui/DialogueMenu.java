package com.eldritch.invoken.ui;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Npc;
import com.eldritch.invoken.actor.type.Player;
import com.eldritch.invoken.screens.AbstractScreen;
import com.eldritch.invoken.util.DefaultInputListener;
import com.eldritch.scifirpg.proto.Actors.DialogueTree.Choice;
import com.eldritch.scifirpg.proto.Actors.DialogueTree.Response;
import com.esotericsoftware.tablelayout.Cell;

public class DialogueMenu {
//	private final Table container;
	private final Table table;
	private final Window bubble;
	private final Skin skin;
	private boolean active = false;
	private Label label;
	
	public DialogueMenu(Skin skin) {
//	    container = new Table(skin);
//	    container.setHeight(AbstractScreen.MENU_VIEWPORT_HEIGHT / 2);
//	    container.setWidth(AbstractScreen.MENU_VIEWPORT_WIDTH);
//		container.bottom();

	    table = new Table(null);
		table.bottom();
		
		this.skin = skin;
		
		ScrollPane scroll = new ScrollPane(table, skin);
//		container.add(scroll).expand().fillX().bottom();
//		container.setVisible(false);
		
		label = new Label("", skin);
		
		bubble = new Window("", skin);
		bubble.add(scroll).expand().fill();
		bubble.setVisible(false);
	}
	
	public void update(Player player, Camera camera) {
		if (player.inDialogue()) {
			if (!active) {
				bubble.setVisible(true);
				label.setVisible(true);
				active = true;
				setup(player.getInteractor());
			}
			setPosition(player.getInteractor(), camera);
		} else {
			endDialogue();
		}
	}
	
	public void draw(Batch batch) {
	    if (active) {
	        table.draw(batch, 1.0f);
	    }
	}
	
	private void setup(Npc npc) {
		setup(npc, npc.getGreeting());
	}
	
	private void setup(Npc npc, Response response) {
		if (response != null) {
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
	
	public Window getTable() {
		return bubble;
	}
	
	private void endDialogue() {
//		container.setVisible(false);
		bubble.setVisible(false);
		label.setVisible(false);
		active = false;
		// TODO: end dialogue with player
	}
	
	private void addLabel(String text) {
		Label label = new Label(text, skin);
		label.setWrap(true);
		label.setWidth(10);
		label.setHeight(10);
		
		@SuppressWarnings("rawtypes")
        Cell cell = table.add(label).minWidth(250).maxWidth(500);
		table.pack(); // defines the text width and thus the preferred height
		cell.height(label.getPrefHeight());
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
	
	private void setPosition(Agent agent, Camera camera) {
	    Vector2 position = agent.getPosition();
        float h = agent.getHeight() / 2;
        float w = agent.getWidth() / 2;
        Vector3 screen = camera.project(new Vector3(position.x - w, position.y + h, 0));
        table.setPosition(screen.x, screen.y);
	}
}
