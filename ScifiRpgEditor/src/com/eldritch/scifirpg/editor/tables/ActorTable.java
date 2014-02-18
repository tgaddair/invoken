package com.eldritch.scifirpg.editor.tables;

import javax.swing.JPanel;

import com.eldritch.scifirpg.editor.actor.CreateActorPanel;

public class ActorTable extends AssetTable {
	private static final long serialVersionUID = 1L;
	private static final String[] COLUMN_NAMES = { 
		"ID", "First Name", "Last Name", "Alias", "Gender", "Level", "Primary", "Secondary" };
	
	public ActorTable() {
		super(COLUMN_NAMES);
	}

	@Override
	protected JPanel getEditorPanel() {
		return new CreateActorPanel();
	}
	
	@Override
	protected String getAssetName() {
		return "Actor";
	}
}
