package com.eldritch.scifirpg.editor.tables;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.eldritch.scifirpg.editor.asset.CreateActorPanel;
import com.eldritch.scifirpg.proto.Actors.NonPlayerActor;
import com.google.common.base.Optional;

public class ActorTable extends AssetTable<NonPlayerActor> {
	private static final long serialVersionUID = 1L;
	private static final String[] COLUMN_NAMES = { 
		"ID", "First Name", "Last Name", "Alias", "Gender", "Level", "Primary", "Secondary" };
	
	public ActorTable() {
		super(COLUMN_NAMES);
	}

	@Override
	protected JPanel getEditorPanel(Optional<NonPlayerActor> prev, JFrame frame) {
		return new CreateActorPanel();
	}
	
	@Override
	protected String getAssetName() {
		return "Actor";
	}
}
