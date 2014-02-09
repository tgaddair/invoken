package com.eldritch.scifirpg.editor.tables;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class ItemTable extends JTable {
	private static final long serialVersionUID = 1L;
	private static final String[] COLUMN_NAMES = { 
		"ID", "Name", "Value", "Droppable" };
	
	public ItemTable() {
		super(new DefaultTableModel(COLUMN_NAMES, 0));
	}
}
