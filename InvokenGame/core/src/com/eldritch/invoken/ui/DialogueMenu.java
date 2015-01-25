package com.eldritch.invoken.ui;

import java.util.List;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Player;
import com.eldritch.invoken.proto.Actors.DialogueTree.Choice;
import com.eldritch.invoken.proto.Actors.DialogueTree.Response;
import com.eldritch.invoken.util.DefaultInputListener;
import com.eldritch.invoken.util.Settings;

public class DialogueMenu {
	private final Table container;
	private final Table choiceBar;
	private final Table bubble;
	private final Skin skin;
	private boolean active = false;
	
	public DialogueMenu(Skin skin) {
	    this.skin = skin;
	    
	    container = new Table(skin);
	    container.setHeight(Settings.MENU_VIEWPORT_HEIGHT / 2);
	    container.setWidth(Settings.MENU_VIEWPORT_WIDTH);
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
	
	private void setup(Agent npc) {
		setup(npc, npc.getDialogueHandler().getGreeting());
	}
	
	private void setup(Agent npc, Response response) {
		if (response != null) {
			// remove old content
		    choiceBar.clear();
		    bubble.clear();
		    
		    List<Choice> choices = npc.getDialogueHandler().getChoicesFor(response);
		    if (choices.isEmpty()) {
		    	// display a text bubble, as there is no choice for the player to make
		    	addLabel(response.getText());
		    	container.setVisible(false);
		    } else {
		    	// use the bubble to indicate that the full conversation appears elsewhere
		    	addLabel("...");
		    	
		    	// use the lower portion of the screen for the dialogue
		    	LabelStyle labelStyle = skin.get("response", LabelStyle.class);
//		    	Label name = new Label(npc.getInfo().getName(), labelStyle);
//		    	choiceBar.add(name).expand().fill();
//		    	choiceBar.row();
		    	
				Label label = new Label(response.getText(), labelStyle);
				label.setWrap(true);
				choiceBar.add(label).expand().fill().padBottom(15);
				choiceBar.row();
				
				// add the choices below
				for (final Choice c : choices) {
					addChoiceButton(c, npc);
				}
		    }
		} else {
			// end of conversation
			container.setVisible(false);
		}
	}
	
	private void endDialogue() {
		container.setVisible(false);
		active = false;
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
	
	private void addChoiceButton(final Choice c, final Agent npc) {
		TextButtonStyle buttonStyle = skin.get("choice", TextButtonStyle.class);
		final TextButton choice = new TextButton(c.getText(), buttonStyle);
		choice.addListener(new DefaultInputListener() {
			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				setup(npc, npc.getDialogueHandler().getResponseFor(c));
			}
		});
		choiceBar.add(choice).left().padLeft(50).padRight(25).padBottom(10);
		choiceBar.row();
	}
	
	private void setPosition(Agent agent, Camera camera) {
	    Vector2 position = agent.getRenderPosition();
        float h = agent.getHeight() / 2;
        float w = agent.getWidth() / 2;
        Vector3 screen = camera.project(new Vector3(position.x - w, position.y + h, 0));
        bubble.setPosition(screen.x, screen.y);
	}
}
