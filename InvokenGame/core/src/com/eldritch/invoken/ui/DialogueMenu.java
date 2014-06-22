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
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Npc;
import com.eldritch.invoken.actor.type.Player;
import com.eldritch.invoken.proto.Actors.DialogueTree.Choice;
import com.eldritch.invoken.proto.Actors.DialogueTree.Response;
import com.eldritch.invoken.screens.AbstractScreen;
import com.eldritch.invoken.util.DefaultInputListener;
import com.esotericsoftware.tablelayout.Cell;

public class DialogueMenu {
	private final Table container;
	private final Table choiceBar;
	private final Table bubble;
	private final Skin skin;
	private boolean active = false;
	
	public DialogueMenu(Skin skin) {
	    this.skin = skin;
	    
	    container = new Table(skin);
	    container.setHeight(AbstractScreen.MENU_VIEWPORT_HEIGHT / 2);
	    container.setWidth(AbstractScreen.MENU_VIEWPORT_WIDTH);
		container.bottom();

	    bubble = new Table(null);
	    bubble.bottom();
		
		choiceBar = new Table(skin);
		choiceBar.bottom();
		
		ScrollPane scroll = new ScrollPane(choiceBar, skin);
		container.add(scroll).expand().fillX().bottom();
		container.setVisible(false);
	}
	
	public Table getTable() {
	    return container;
	}
	
	public void update(Player player, Camera camera) {
		if (player.inDialogue()) {
			if (!active) {
			    container.setVisible(true);
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
	        bubble.draw(batch, 1.0f);
	    }
	}
	
	private void setup(Npc npc) {
		setup(npc, npc.getGreeting());
	}
	
	private void setup(Npc npc, Response response) {
		if (response != null) {
			// remove old content
		    choiceBar.clear();
		    bubble.clear();
			
			// add new content
			addLabel(response.getText());
			
			boolean hasChoice = false;
			for (final Choice c : npc.getChoicesFor(response)) {
				addChoiceButton(c, npc);
				hasChoice = true;
			}
			if (!hasChoice) {
			    container.setVisible(false);
			}
		} else {
			// end of conversation
			endDialogue();
		}
	}
	
	private void endDialogue() {
		container.setVisible(false);
		active = false;
		// TODO: end dialogue with player
	}
	
	private void addLabel(String text) {
		Label label = new Label(text, skin);
		label.setWrap(true);
		label.setWidth(10);
		label.setHeight(10);
		
		@SuppressWarnings("rawtypes")
        Cell cell = bubble.add(label).minWidth(250).maxWidth(500);
		bubble.pack(); // defines the text width and thus the preferred height
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
		choiceBar.add(choice).left().padLeft(25).padRight(25).padBottom(10);
		choiceBar.row();
	}
	
	private void setPosition(Agent agent, Camera camera) {
	    Vector2 position = agent.getPosition();
        float h = agent.getHeight() / 2;
        float w = agent.getWidth() / 2;
        Vector3 screen = camera.project(new Vector3(position.x - w, position.y + h, 0));
        bubble.setPosition(screen.x, screen.y);
	}
}
