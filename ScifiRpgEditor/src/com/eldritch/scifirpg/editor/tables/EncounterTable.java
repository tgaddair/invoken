package com.eldritch.scifirpg.editor.tables;

import javax.swing.JPanel;

public class EncounterTable extends AssetTable {
	private static final long serialVersionUID = 1L;
	private static final String[] COLUMN_NAMES = { 
		"ID", "Title", "Weight", "Unique" };
	
	public EncounterTable() {
		super(COLUMN_NAMES);
	}

	@Override
	protected JPanel getEditorPanel() {
		return new JPanel();
	}
}
