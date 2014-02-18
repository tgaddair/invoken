package com.eldritch.scifirpg.editor.tables;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import java.awt.Dimension;
import java.awt.GridLayout;

public class AssetTablePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	public AssetTablePanel(final JTable table) {
		super(new GridLayout(1, 0));

		table.setPreferredScrollableViewportSize(new Dimension(500, 70));
		table.setFillsViewportHeight(true);

		// Create the scroll pane and add the table to it.
		JScrollPane scrollPane = new JScrollPane(table);

		// Add the scroll pane to this panel.
		add(scrollPane);
	}
}
