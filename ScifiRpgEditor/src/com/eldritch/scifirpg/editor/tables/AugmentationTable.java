package com.eldritch.scifirpg.editor.tables;

import javax.swing.JPanel;

import com.eldritch.scifirpg.editor.actor.AugmentationEditorPanel;

public class AugmentationTable extends AssetTable {
	private static final long serialVersionUID = 1L;
	private static final String[] COLUMN_NAMES = { 
		"ID", "Name", "Value", "Slots", "Discipline" };
	
	public AugmentationTable() {
		super(COLUMN_NAMES);
	}

	@Override
	protected JPanel getEditorPanel() {
		return new AugmentationEditorPanel();
	}
}
