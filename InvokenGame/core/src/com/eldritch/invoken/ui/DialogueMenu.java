package com.eldritch.invoken.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;

public class DialogueMenu {
	private final Table table;
	
	public DialogueMenu(Skin skin) {
		Label nameLabel = new Label("Name:", skin);
	    TextField nameText = new TextField("", skin);
	    Label addressLabel = new Label("Address:", skin);
	    TextField addressText = new TextField("", skin);

	    table = new Table(skin);
		table.setFillParent(true);
		table.bottom();
		
//	    table.add(nameLabel).expandX();
//	    table.add(nameText).width(100);
//	    table.row();
//	    table.add(addressLabel).expandX();
//	    table.add(addressText).width(100);
		
		Label textLabel = new Label("This is a test showing a really long line of dialogue that might be seen in the game when speaking to some random NPC you encounter.", skin);
		textLabel.setWrap(true);
		table.add(textLabel).left().expandX().height(100);
	}
	
	public Table getTable() {
		return table;
	}
}
