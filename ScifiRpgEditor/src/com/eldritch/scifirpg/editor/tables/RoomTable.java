package com.eldritch.scifirpg.editor.tables;

import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.eldritch.invoken.proto.Locations.Room;
import com.eldritch.invoken.proto.Locations.Room.Furniture;
import com.eldritch.invoken.proto.Locations.Room.Furniture.Type;
import com.eldritch.invoken.proto.Locations.Room.Size;
import com.eldritch.scifirpg.editor.AssetTablePanel;
import com.eldritch.scifirpg.editor.panel.AssetEditorPanel;
import com.google.common.base.Optional;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class RoomTable extends MajorAssetTable<Room> {
	private static final long serialVersionUID = 1L;
	private static final String[] COLUMN_NAMES = { 
		"ID", "Size" };
	
	public RoomTable() {
		super(COLUMN_NAMES, "Room");
	}

	@Override
	protected JPanel getEditorPanel(Optional<Room> prev, JFrame frame) {
		return new RoomEditorPanel(this, frame, prev);
	}
	
	@Override
	protected Object[] getDisplayFields(Room asset) {
		return new Object[]{asset.getId(), asset.getSize().name()};
	}

	@Override
	protected String getAssetDirectory() {
		return "rooms";
	}

	@Override
	protected Room readFrom(InputStream is) throws IOException {
		return Room.parseFrom(is);
	}

	@Override
	protected String getAssetId(Room asset) {
		return asset.getId();
	}
	
	private class RoomEditorPanel extends AssetEditorPanel<Room, RoomTable> {
		private static final long serialVersionUID = 1L;

		private final JTextField idField = new JTextField();
		private final JComboBox<Size> sizeBox = new JComboBox<Size>(Size.values());
		private final FurnitureTable furnitureTable = new FurnitureTable();

		public RoomEditorPanel(RoomTable owner, JFrame frame, Optional<Room> prev) {
			super(owner, frame, prev);

			DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(""));
			builder.border(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			builder.appendColumn("right:pref");
			builder.appendColumn("3dlu");
			builder.appendColumn("fill:max(pref; 100px)");
			
			builder.append("ID:", idField);
			builder.nextLine();
			
			builder.append("Size:", sizeBox);
			builder.nextLine();
			
			builder.appendRow("fill:p:grow");
			builder.append("Furniture:", new AssetTablePanel(furnitureTable));
			builder.nextLine();

			JButton saveButton = new JButton("Save");
			saveButton.addActionListener(this);
			builder.append(saveButton);
			builder.nextLine();
			
			if (prev.isPresent()) {
				Room room = prev.get();
				idField.setText(room.getId());
				sizeBox.setSelectedItem(room.getSize());
				for (Furniture f : room.getFurnitureList()) {
					furnitureTable.addAsset(f);
				}
			}

			add(builder.getPanel());
			setPreferredSize(new Dimension(650, 750));
		}

		@Override
		public Room createAsset() {
			return Room.newBuilder()
					.setId(idField.getText())
					.setSize((Size) sizeBox.getSelectedItem())
					.addAllFurniture(furnitureTable.getAssets())
					.build();
		}
	}
	
	public static class FurnitureTable extends IdentifiedAssetTable<Furniture> {
		private static final long serialVersionUID = 1L;
		private static final String[] COLUMN_NAMES = { 
			"ID", "Type" };
		
		public FurnitureTable() {
			super(COLUMN_NAMES, "Furniture");
		}

		@Override
		protected JPanel getEditorPanel(Optional<Furniture> prev, JFrame frame) {
			return new FurnitureEditorPanel(this, frame, prev);
		}
		
		@Override
		protected Object[] getDisplayFields(Furniture asset) {
			return new Object[]{asset.getId(), asset.getType()};
		}

		@Override
		protected String getAssetId(Furniture asset) {
			return asset.getId();
		}
	}
	
	public static class FurnitureEditorPanel extends AssetEditorPanel<Furniture, FurnitureTable> {
		private static final long serialVersionUID = 1L;

		private final JTextField idField = new JTextField();
		private final JComboBox<Type> typeBox = new JComboBox<Type>(Type.values());

		public FurnitureEditorPanel(FurnitureTable owner, JFrame frame, Optional<Furniture> prev) {
			super(owner, frame, prev);

			DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(""));
			builder.border(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			builder.appendColumn("right:pref");
			builder.appendColumn("3dlu");
			builder.appendColumn("fill:max(pref; 100px)");

			builder.append("ID:", idField);
			builder.nextLine();

			builder.append("Type:", typeBox);
			builder.nextLine();

			JButton saveButton = new JButton("Save");
			saveButton.addActionListener(this);
			builder.append(saveButton);
			builder.nextLine();
			
			if (prev.isPresent()) {
				Furniture asset = prev.get();
				idField.setText(asset.getId() + "");
				typeBox.setSelectedItem(asset.getType());
			}

			add(builder.getPanel());
		}

		@Override
		public Furniture createAsset() {
			return Furniture.newBuilder()
					.setId(idField.getText())
					.setType((Type) typeBox.getSelectedItem())
					.build();
		}
	}
}
