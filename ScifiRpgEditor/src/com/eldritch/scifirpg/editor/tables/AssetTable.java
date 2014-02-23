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
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import com.google.common.base.Optional;
import com.google.protobuf.Message;

public abstract class AssetTable<T extends Message> extends JTable {
	private static final long serialVersionUID = 1L;
	
	private final JPopupMenu popup;
	private final String[] columnNames;
	private final String assetName;
	private final JMenuItem deleteItem;
	
	public AssetTable(String[] columnNames, String assetName) {
		super(new AssetTableModel<T>(columnNames));
		this.columnNames = columnNames;
		this.assetName = assetName;
		
		// Create the popup menu.
	    popup = new JPopupMenu();
	    JMenuItem menuItem = new JMenuItem("Create New " + assetName);
	    menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				handleCreateAsset(Optional.<T>absent());
			}
	    });
	    popup.add(menuItem);
	    
	    deleteItem = new JMenuItem("Delete Selected " + assetName);
	    deleteItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				handleDeleteAsset(AssetTable.this.getSelectedRow());
			}
	    });
	    popup.add(deleteItem);
	    deleteItem.setVisible(false);

	    // Add listener to components that can bring up popup menus.
	    MouseListener popupListener = new PopupListener();
	    addMouseListener(popupListener);
	    
	    addMouseListener(new MouseAdapter() {
	        public void mousePressed(MouseEvent me) {
	        	deleteItem.setVisible(AssetTable.this.getSelectedRow() >= 0);
	        	
	            JTable table = (JTable) me.getSource();
	            Point p = me.getPoint();
	            int row = table.rowAtPoint(p);
	            
	            if (me.getClickCount() == 2) {
	            	if (row >= 0) {
	            		T asset = getModel().getAsset(row);
	            		handleCreateAsset(Optional.<T>of(asset));
	            	} else {
	            		handleCreateAsset(Optional.<T>absent());
	            	}
	            }
	        }
	    });
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public AssetTableModel<T> getModel() {
		return (AssetTableModel<T>) super.getModel();
	}
	
	public String[] getColumnNames() {
		return columnNames;
	}
	
	public String getAssetName() {
		return assetName;
	}
	
	protected void handleCreateAsset(Optional<T> asset) {
		// Create and set up the window.
        JFrame frame = new JFrame(assetName + " Editor");
        frame.add(getEditorPanel(asset, frame), BorderLayout.CENTER);
        
        // Display the window.
        frame.pack();
        frame.setVisible(true);
	}
	
	protected void handleDeleteAsset(int row) {
		getModel().removeAssetAt(row);
	}
	
	protected abstract JPanel getEditorPanel(Optional<T> asset, JFrame frame);
	
	protected abstract Object[] getDisplayFields(T asset);
	
	public void addAsset(T asset) {
		addAsset(Optional.<T>absent(), asset);
	}
	
	public void addAsset(Optional<T> prev, T asset) {
		getModel().addAsset(prev, asset, getDisplayFields(asset));
	}
	
	public void saveAsset(Optional<T> prev, T asset) {
		addAsset(prev, asset);
	}
	
	public List<T> getAssets() {
		return getModel().getAssets();
	}
	
	public void clearAssets() {
		setModel(new AssetTableModel<T>(columnNames));
	}
	
	public static class AssetTableModel<T extends Message> extends DefaultTableModel {
		private static final long serialVersionUID = 1L;
		private final List<T> assets;
		
		public AssetTableModel(String[] columnNames) {
			super(columnNames, 0);
			assets = new ArrayList<T>();
		}
		
		public void addAsset(Optional<T> prev, T asset, Object[] data) {
			if (prev.isPresent()) {
				int i = assets.indexOf(prev.get());
				assets.set(i, asset);
				for (int j = 0; j < data.length; j++) {
					setValueAt(data[j], i, j);
				}
			} else {
				assets.add(asset);
				addRow(data);
			}
		}
		
		public List<T> getAssets() {
			return assets;
		}
		
		public T getAsset(int i) {
			return assets.get(i);
		}
		
		public void removeAssetAt(int row) {
			assets.remove(row);
			removeRow(row);
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
