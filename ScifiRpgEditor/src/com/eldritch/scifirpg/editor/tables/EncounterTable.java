package com.eldritch.scifirpg.editor.tables;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class EncounterTable extends JTable {
	private static final long serialVersionUID = 1L;
	private static final String[] COLUMN_NAMES = { 
		"ID", "Title", "Weight", "Unique" };
	
	public EncounterTable() {
		super(new DefaultTableModel(COLUMN_NAMES, 0));
	}
}
