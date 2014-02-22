package com.eldritch.scifirpg.editor.tables;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.eldritch.scifirpg.editor.panel.AssetEditorPanel;
import com.google.common.base.Optional;
import com.google.protobuf.Message;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class AssetPointerTable<T extends Message> extends AssetTable<T> {
	private static final long serialVersionUID = 1L;
	
	private final MajorAssetTable<T> majorTable;
	
	public AssetPointerTable(MajorAssetTable<T> table) {
		super(table.getColumnNames(), table.getAssetName());
		this.majorTable = table;
	}
	
	public Set<String> getAssetIds() {
		Set<String> ids = new HashSet<>();
		for (T asset : getAssets()) {
			ids.add(majorTable.getAssetId(asset));
		}
		return ids;
	}
	
	public void addAssetId(String id) {
		for (T asset : majorTable.getAssets()) {
			if (majorTable.getAssetId(asset).equals(id)) {
				addAsset(asset);
			}
		}
	}

	@Override
	protected JPanel getEditorPanel(Optional<T> asset, JFrame frame) {
		return new PointerEditorPanel(this, frame, asset);
	}

	@Override
	protected Object[] getDisplayFields(T asset) {
		return majorTable.getDisplayFields(asset);
	}
	
	private class PointerEditorPanel extends AssetEditorPanel<T, AssetTable<T>> {
		private static final long serialVersionUID = 1L;
		
		private final JComboBox<String> pointerBox = new JComboBox<String>();
		
		public PointerEditorPanel(AssetTable<T> table, JFrame frame, Optional<T> prev) {
			super(table, frame, prev);
			
			DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(""));
			builder.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			builder.appendColumn("right:pref");
			builder.appendColumn("3dlu");
			builder.appendColumn("fill:max(pref; 100px)");
			
			Set<String> currentIds = getAssetIds();
			List<String> values = new ArrayList<>();
			for (String id : majorTable.getAssetIds()) {
				if ((prev.isPresent() && majorTable.getAssetId(prev.get()).equals(id))
						|| !currentIds.contains(id)) {
					values.add(id);
				}
			}
			pointerBox.setModel(new DefaultComboBoxModel<String>(values.toArray(new String[0])));
			builder.append("ID:", pointerBox);
			builder.nextLine();

			JButton saveButton = new JButton("Save");
			saveButton.addActionListener(this);
			builder.append(saveButton);
			builder.nextLine();
			
			if (prev.isPresent()) {
				T asset = prev.get();
				pointerBox.setSelectedItem(majorTable.getAssetId(asset));
			}

			add(builder.getPanel());
		}

		@Override
		public T createAsset() {
			String id = (String) pointerBox.getSelectedItem();
			return majorTable.getAssetFor(id);
		}
	}
}
