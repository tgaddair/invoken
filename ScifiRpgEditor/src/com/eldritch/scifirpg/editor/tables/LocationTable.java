package com.eldritch.scifirpg.editor.tables;

import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.eldritch.scifirpg.editor.AssetTablePanel;
import com.eldritch.scifirpg.editor.panel.AssetEditorPanel;
import com.eldritch.scifirpg.proto.Locations.Encounter;
import com.eldritch.scifirpg.proto.Locations.Location;
import com.google.common.base.Optional;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class LocationTable extends MajorAssetTable<Location> {
	private static final long serialVersionUID = 1L;
	private static final String[] COLUMN_NAMES = { 
		"ID", "Name", "Parent", "Encounters" };
	
	public LocationTable() {
		super(COLUMN_NAMES, "Location");
	}

	@Override
	protected JPanel getEditorPanel(Optional<Location> prev, JFrame frame) {
		return new LocationEditorPanel(this, frame, prev);
	}
	
	@Override
	protected Object[] getDisplayFields(Location asset) {
		String encounters = "";
		for (Encounter e : asset.getEncounterList()) {
			encounters += e.getId() + " ";
		}
		Object parentId = asset.hasParentId() ? asset.getParentId() : "";
		return new Object[]{asset.getId(), asset.getName(), parentId, encounters};
	}

	@Override
	protected String getAssetDirectory() {
		return "locations";
	}

	@Override
	protected Location readFrom(InputStream is) throws IOException {
		return Location.parseFrom(is);
	}

	@Override
	protected String getAssetId(Location asset) {
		return asset.getId();
	}
	
	private class LocationEditorPanel extends AssetEditorPanel<Location, LocationTable> {
		private static final long serialVersionUID = 1L;

		private final JTextField idField = new JTextField();
		private final JTextField nameField = new JTextField();
		private final JComboBox<String> parentBox = new JComboBox<String>();
		private final EncounterTable encounterTable = new EncounterTable();

		public LocationEditorPanel(LocationTable owner, JFrame frame, Optional<Location> prev) {
			super(owner, frame, prev);

			DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(""));
			builder.border(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			builder.appendColumn("right:pref");
			builder.appendColumn("3dlu");
			builder.appendColumn("fill:max(pref; 100px)");
			
			nameField.addActionListener(new NameTypedListener(idField));
			builder.append("Name:", nameField);
			builder.nextLine();
			
			builder.append("ID:", idField);
			builder.nextLine();
			
			List<String> values = new ArrayList<>();
			values.add("");
			for (String id : owner.getAssetIds()) {
				if (!prev.isPresent() || !prev.get().getId().equals(id)) {
					values.add(id);
				}
			}
			parentBox.setModel(new DefaultComboBoxModel<String>(values.toArray(new String[0])));
			builder.append("Parent:", parentBox);
			builder.nextLine();
			
			builder.appendRow("fill:p:grow");
			builder.append("Encounters:", new AssetTablePanel(encounterTable));
			builder.nextLine();

			JButton saveButton = new JButton("Save");
			saveButton.addActionListener(this);
			builder.append(saveButton);
			builder.nextLine();
			
			if (prev.isPresent()) {
				Location loc = prev.get();
				idField.setText(loc.getId());
				nameField.setText(loc.getName());
				if (loc.hasParentId()) {
					parentBox.setSelectedItem(loc.getParentId());
				}
				for (Encounter e : loc.getEncounterList()) {
					encounterTable.addAsset(e);
				}
			}

			add(builder.getPanel());
			setPreferredSize(new Dimension(650, 750));
		}

		@Override
		public Location createAsset() {
			Location.Builder location = Location.newBuilder()
					.setId(idField.getText())
					.setName(nameField.getText())
					.addAllEncounter(encounterTable.getAssets());
			String parentId = (String) parentBox.getSelectedItem();
			if (!parentId.isEmpty()) {
				location.setParentId(parentId);
			}
			return location.build();
		}
	}
}
