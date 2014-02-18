package com.eldritch.scifirpg.editor.tables;

import javax.swing.JPanel;

public class ItemTable extends AssetTable {
	private static final long serialVersionUID = 1L;
	private static final String[] COLUMN_NAMES = { 
		"ID", "Name", "Value", "Droppable" };
	
	public ItemTable() {
		super(COLUMN_NAMES);
	}

	@Override
	protected JPanel getEditorPanel() {
		return new JPanel();
	}
}
