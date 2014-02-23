package com.eldritch.scifirpg.editor.tables;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.eldritch.scifirpg.editor.panel.ResponseEditorPanel;
import com.eldritch.scifirpg.proto.Actors.DialogueTree.Response;
import com.google.common.base.Optional;

public class DialogueTable extends IdentifiedAssetTable<Response> {
	private static final long serialVersionUID = 1L;
	private static final String[] COLUMN_NAMES = { 
		"ID", "Text", "Greeting" };
	
	public DialogueTable() {
		super(COLUMN_NAMES, "Response");
	}

	@Override
	protected JPanel getEditorPanel(Optional<Response> prev, JFrame frame) {
		return new ResponseEditorPanel(this, frame, prev);
	}
	
	@Override
	protected Object[] getDisplayFields(Response resp) {
		return new Object[]{resp.getId(), resp.getText(), resp.getGreeting()};
	}

	@Override
	protected String getAssetId(Response asset) {
		return asset.getId();
	}
}
