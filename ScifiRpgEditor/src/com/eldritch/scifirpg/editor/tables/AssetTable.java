package com.eldritch.scifirpg.editor.tables;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;

import com.eldritch.scifirpg.editor.asset.CreateActorPanel;
import com.google.common.base.Optional;
import com.google.protobuf.Message;

public abstract class AssetTable<T extends Message> extends JTable {
	private static final long serialVersionUID = 1L;
	
	private final JPopupMenu popup;
	
	public AssetTable(String[] columnNames) {
		super(new AssetTableModel<T>(columnNames));
		
		// Create the popup menu.
	    popup = new JPopupMenu();
	    JMenuItem menuItem = new JMenuItem("Create New " + getAssetName());
	    menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				handleCreateAsset(Optional.<T>absent());
			}
	    });
	    popup.add(menuItem);

	    // Add listener to components that can bring up popup menus.
	    MouseListener popupListener = new PopupListener();
	    addMouseListener(popupListener);
	    
	    addMouseListener(new MouseAdapter() {
	        public void mousePressed(MouseEvent me) {
	            JTable table = (JTable) me.getSource();
	            Point p = me.getPoint();
	            int row = table.rowAtPoint(p);
	            if (me.getClickCount() == 2 && row >= 0) {
	                T asset = getModel().getAsset(row);
	                handleCreateAsset(Optional.<T>of(asset));
	            }
	        }
	    });
	}
	
	@Override
	public AssetTableModel<T> getModel() {
		return (AssetTableModel<T>) super.getModel();
	}
	
	protected void handleCreateAsset(Optional<T> asset) {
		// Create and set up the window.
        JFrame frame = new JFrame("Create New " + getAssetName());
        frame.add(getEditorPanel(asset, frame), BorderLayout.CENTER);
        
        // Display the window.
        frame.pack();
        frame.setVisible(true);
	}
	
	protected abstract JPanel getEditorPanel(Optional<T> asset, JFrame frame);
	
	protected abstract String getAssetName();
	
	public static class AssetTableModel<T extends Message> extends DefaultTableModel {
		private final List<T> assets;
		
		public AssetTableModel(String[] columnNames) {
			super(columnNames, 0);
			assets = new ArrayList<T>();
		}
		
		public void addAsset(T asset, Object[] data) {
			assets.add(asset);
			addRow(data);
		}
		
		public T getAsset(int i) {
			return assets.get(i);
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
