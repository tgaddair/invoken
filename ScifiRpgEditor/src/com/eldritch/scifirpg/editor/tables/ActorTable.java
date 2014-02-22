package com.eldritch.scifirpg.editor.tables;

import java.io.IOException;
import java.io.InputStream;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.eldritch.scifirpg.editor.asset.ActorEditorPanel;
import com.eldritch.scifirpg.proto.Actors.NonPlayerActor;
import com.google.common.base.Optional;

public class ActorTable extends MajorAssetTable<NonPlayerActor> {
	private static final long serialVersionUID = 1L;
	private static final String[] COLUMN_NAMES = { 
		"ID", "First Name", "Last Name", "Alias", "Gender", "Level", "Primary", "Secondary" };
	
	public ActorTable() {
		super(COLUMN_NAMES, "Actor");
	}

	@Override
	protected JPanel getEditorPanel(Optional<NonPlayerActor> prev, JFrame frame) {
		return new ActorEditorPanel(this, frame, prev);
	}

	@Override
	protected Object[] getDisplayFields(NonPlayerActor asset) {
		return new Object[0];
	}

	@Override
	protected String getAssetDirectory() {
		return "actors";
	}

	@Override
	protected String getAssetId(NonPlayerActor asset) {
		return asset.getParams().getId();
	}

	@Override
	protected NonPlayerActor readFrom(InputStream is) throws IOException {
		return NonPlayerActor.parseFrom(is);
	}
}
