package com.eldritch.scifirpg.editor.tables;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.google.common.base.Optional;
import com.google.protobuf.Message;

public class EncounterTable extends AssetTable<Message> {
	private static final long serialVersionUID = 1L;
	private static final String[] COLUMN_NAMES = { 
		"ID", "Title", "Weight", "Unique" };
	
	public EncounterTable() {
		super(COLUMN_NAMES);
	}

	@Override
	protected JPanel getEditorPanel(Optional<Message> prev, JFrame frame) {
		return new JPanel();
	}
	
	@Override
	protected String getAssetName() {
		return "Encounter";
	}
	
	@Override
	protected Object[] getDisplayFields(Message asset) {
		return new Object[0];
	}
}
