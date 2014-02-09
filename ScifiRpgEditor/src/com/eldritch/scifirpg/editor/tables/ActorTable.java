package com.eldritch.scifirpg.editor.tables;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class ActorTable extends JTable {
	private static final long serialVersionUID = 1L;
	private static final String[] COLUMN_NAMES = { 
		"ID", "First Name", "Last Name", "Alias", "Gender", "Level", "Primary", "Secondary" };
	
	public ActorTable() {
		super(new DefaultTableModel(COLUMN_NAMES, 0));
	}
}
