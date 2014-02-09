package com.eldritch.scifirpg.editor.tables;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class AssetTablePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private final JPopupMenu popup;

	public AssetTablePanel(final JTable table) {
		super(new GridLayout(1, 0));

		table.setPreferredScrollableViewportSize(new Dimension(500, 70));
		table.setFillsViewportHeight(true);

		table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				printDebugData(table);
			}
		});
		
		// Create the popup menu.
	    popup = new JPopupMenu();
	    JMenuItem menuItem = new JMenuItem("New Asset");
	    //menuItem.addActionListener(this);
	    popup.add(menuItem);

	    // Add listener to components that can bring up popup menus.
	    MouseListener popupListener = new PopupListener();
	    table.addMouseListener(popupListener);

		// Create the scroll pane and add the table to it.
		JScrollPane scrollPane = new JScrollPane(table);

		// Add the scroll pane to this panel.
		add(scrollPane);
	}

	private void printDebugData(JTable table) {
		int numRows = table.getRowCount();
		int numCols = table.getColumnCount();
		javax.swing.table.TableModel model = table.getModel();

		System.out.println("Value of data: ");
		for (int i = 0; i < numRows; i++) {
			System.out.print("    row " + i + ":");
			for (int j = 0; j < numCols; j++) {
				System.out.print("  " + model.getValueAt(i, j));
			}
			System.out.println();
		}
		System.out.println("--------------------------");
	}
	
	private class PopupListener extends MouseAdapter {
	    public void mousePressed(MouseEvent e) {
	        maybeShowPopup(e);
	    }

	    public void mouseReleased(MouseEvent e) {
	        maybeShowPopup(e);
	    }

	    private void maybeShowPopup(MouseEvent e) {
	        if (e.isPopupTrigger()) {
	            popup.show(e.getComponent(),
	                       e.getX(), e.getY());
	        }
	    }
	}
}
