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
import com.eldritch.scifirpg.editor.MainPanel;
import com.eldritch.scifirpg.editor.panel.AssetEditorPanel;
import com.eldritch.invoken.proto.Locations.Biome;
import com.eldritch.invoken.proto.Locations.Encounter;
import com.eldritch.invoken.proto.Locations.Light;
import com.eldritch.invoken.proto.Locations.Location;
import com.eldritch.invoken.proto.Locations.Room;
import com.google.common.base.Optional;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class LocationTable extends MajorAssetTable<Location> {
	private static final long serialVersionUID = 1L;
	private static final String[] COLUMN_NAMES = { 
		"ID", "Name", "Faction", "Encounters" };
	
	public LocationTable() {
		super(COLUMN_NAMES, "Location");
	}

	@Override
	protected JPanel getEditorPanel(Optional<Location> prev, JFrame frame) {
		return new LocationEditorPanel(this, frame, prev);
	}
	
	@Override
	protected Object[] getDisplayFields(Location asset) {
		String faction = "";
		if (asset.hasFactionId()) {
			faction = asset.getFactionId();
			if (asset.hasMinRank()) {
				faction += " (" + asset.getMinRank() + ")";
			}
		}
		String encounters = "";
		for (Encounter e : asset.getEncounterList()) {
			encounters += e.getId() + " ";
		}
		return new Object[]{asset.getId(), asset.getName(), faction, encounters};
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
		private final JComboBox<String> factionBox = new JComboBox<String>();
		private final JTextField minRankField = new JTextField();
		private final JComboBox<Biome> biomeBox = new JComboBox<Biome>(Biome.values());
		private final JTextField intensityField = new JTextField("1.0f");
		private final JTextField colorField = new JTextField("255 255 255");
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
			
			List<String> fIds = new ArrayList<>();
			fIds.add("");
			fIds.addAll(MainPanel.FACTION_TABLE.getAssetIds());
			factionBox.setModel(new DefaultComboBoxModel<String>(fIds.toArray(new String[0])));
			builder.append("Faction:", factionBox);
			builder.nextLine();
			
			builder.append("Min Rank:", minRankField);
			builder.nextLine();
			
			builder.append("Biome:", biomeBox);
			builder.nextLine();
			
			builder.append("Ambient Intensity:", intensityField);
			builder.nextLine();
			
			builder.append("Ambient RGB:", colorField);
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
				if (loc.hasFactionId()) {
					factionBox.setSelectedItem(loc.getFactionId());
				}
				if (loc.hasMinRank()) {
					minRankField.setText(loc.getMinRank() + "");
				}
				biomeBox.setSelectedItem(loc.getBiome());
				if (loc.hasLight()) {
					Light light = loc.getLight();
					intensityField.setText(light.getIntensity() + "");
					colorField.setText(light.getR() + " " + light.getG() + " " + light.getB());
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
			String[] rgb = colorField.getText().split(" ");
			Location.Builder location = Location.newBuilder()
					.setId(idField.getText())
					.setName(nameField.getText())
					.setBiome((Biome) biomeBox.getSelectedItem())
					.setLight(Light.newBuilder()
							.setIntensity(Float.parseFloat(intensityField.getText()))
							.setR(Integer.parseInt(rgb[0]))
							.setG(Integer.parseInt(rgb[1]))
							.setB(Integer.parseInt(rgb[2]))
							.build())
					.addAllEncounter(encounterTable.getAssets());
			String factionId = (String) factionBox.getSelectedItem();
			if (!factionId.isEmpty()) {
				location.setFactionId(factionId);
				if (!minRankField.getText().isEmpty()) {
					location.setMinRank(Integer.parseInt(minRankField.getText()));
				}
			}
			return location.build();
		}
	}
}
