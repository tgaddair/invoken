package com.eldritch.scifirpg.editor.tables;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;

import com.eldritch.scifirpg.editor.asset.CreateActorPanel;

public abstract class AssetTable extends JTable {
	private static final long serialVersionUID = 1L;
	
	private final JPopupMenu popup;
	
	public AssetTable(String[] columnNames) {
		super(new AssetTableModel(columnNames));
		
		// Create the popup menu.
	    popup = new JPopupMenu();
	    JMenuItem menuItem = new JMenuItem("Create New " + getAssetName());
	    menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				handleCreateAsset();
			}
	    });
	    popup.add(menuItem);

	    // Add listener to components that can bring up popup menus.
	    MouseListener popupListener = new PopupListener();
	    addMouseListener(popupListener);
	}
	
	protected void handleCreateAsset() {
		// Create and set up the window.
        JFrame frame = new JFrame("Create New " + getAssetName());
        frame.add(getEditorPanel(frame), BorderLayout.CENTER);
        
        // Display the window.
        frame.pack();
        frame.setVisible(true);
	}
	
	protected abstract JPanel getEditorPanel(JFrame frame);
	
	protected abstract String getAssetName();
	
	private static class AssetTableModel extends DefaultTableModel {
		public AssetTableModel(String[] columnNames) {
			super(columnNames, 0);
		}
		
		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}
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
