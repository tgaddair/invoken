package com.eldritch.scifirpg.editor.tables;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.eldritch.invoken.proto.Locations.Room;
import com.eldritch.invoken.proto.Locations.Room.Furniture;
import com.eldritch.invoken.proto.Locations.Room.Furniture.Type;
import com.eldritch.invoken.proto.Locations.Room.Size;
import com.eldritch.scifirpg.editor.AssetTablePanel;
import com.eldritch.scifirpg.editor.MainPanel;
import com.eldritch.scifirpg.editor.panel.AssetEditorPanel;
import com.google.common.base.Optional;
import com.google.protobuf.TextFormat;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class RoomTable extends MajorAssetTable<Room> {
    private static final long serialVersionUID = 1L;
    private static final String[] COLUMN_NAMES = { "ID", "Size" };

    public RoomTable() {
        super(COLUMN_NAMES, "Room");
    }

    @Override
    protected JPanel getEditorPanel(Optional<Room> prev, JFrame frame) {
        return new RoomEditorPanel(this, frame, prev);
    }

    @Override
    protected Object[] getDisplayFields(Room asset) {
        return new Object[] { asset.getId(), asset.getSize().name() };
    }

    @Override
    protected String getAssetDirectory() {
        return "rooms";
    }

    @Override
    protected Room readFromBinary(InputStream is) throws IOException {
        return Room.parseFrom(is);
    }

    @Override
    protected Room readFromText(InputStream is) throws IOException {
        Room.Builder builder = Room.newBuilder();
        TextFormat.merge(new InputStreamReader(is), builder);
        return builder.build();
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
        private final JCheckBox uniqueCheck = new JCheckBox();

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

            builder.append("Unique:", uniqueCheck);
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
                uniqueCheck.setSelected(room.getUnique());
                for (Furniture f : room.getFurnitureList()) {
                    furnitureTable.addAsset(f);
                }
            }

            add(builder.getPanel());
            setPreferredSize(new Dimension(650, 750));
        }

        @Override
        public Room createAsset() {
            return Room.newBuilder().setId(idField.getText())
                    .setSize((Size) sizeBox.getSelectedItem()).setUnique(uniqueCheck.isSelected())
                    .addAllFurniture(furnitureTable.getAssets()).build();
        }
    }

    public static class FurnitureTable extends IdentifiedAssetTable<Furniture> {
        private static final long serialVersionUID = 1L;
        private static final String[] COLUMN_NAMES = { "ID", "Type" };

        public FurnitureTable() {
            super(COLUMN_NAMES, "Furniture");
        }

        @Override
        protected JPanel getEditorPanel(Optional<Furniture> prev, JFrame frame) {
            return new FurnitureEditorPanel(this, frame, prev);
        }

        @Override
        protected Object[] getDisplayFields(Furniture asset) {
            return new Object[] { asset.getId(), asset.getType() };
        }

        @Override
        protected String getAssetId(Furniture asset) {
            return asset.getId();
        }
    }

    public static class FurnitureEditorPanel extends AssetEditorPanel<Furniture, FurnitureTable> {
        private static final long serialVersionUID = 1L;

        private final JComboBox<String> idBox = new JComboBox<>();
        private final JComboBox<Type> typeBox = new JComboBox<>(Type.values());
        private final JComboBox<String> assetBox = new JComboBox<>();
        private final JTextField minField = new JTextField();
        private final JTextField maxField = new JTextField();

        public FurnitureEditorPanel(FurnitureTable owner, JFrame frame, Optional<Furniture> prev) {
            super(owner, frame, prev);

            DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(""));
            builder.border(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            builder.appendColumn("right:pref");
            builder.appendColumn("3dlu");
            builder.appendColumn("fill:max(pref; 100px)");

            idBox.setModel(new DefaultComboBoxModel<String>(getFurnitureList().toArray(
                    new String[0])));
            builder.append("ID:", idBox);
            builder.nextLine();

            typeBox.setSelectedItem(Type.TMX);
            typeBox.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent event) {
                    if (event.getStateChange() == ItemEvent.SELECTED) {
                        Type item = (Type) event.getItem();
                        assetBox.setModel(new DefaultComboBoxModel<>(getAssets(item).toArray(
                                new String[0])));
                    }
                }
            });
            builder.append("Type:", typeBox);
            builder.nextLine();

            assetBox.setModel(new DefaultComboBoxModel<>(
                    getAssets((Type) typeBox.getSelectedItem()).toArray(new String[0])));
            builder.append("Asset:", assetBox);
            builder.nextLine();

            builder.append("Min:", minField);
            builder.nextLine();

            builder.append("Max:", maxField);
            builder.nextLine();

            JButton saveButton = new JButton("Save");
            saveButton.addActionListener(this);
            builder.append(saveButton);
            builder.nextLine();

            if (prev.isPresent()) {
                Furniture asset = prev.get();
                idBox.setSelectedItem(asset.getId());
                typeBox.setSelectedItem(asset.getType());
                if (asset.hasAssetId()) {
                    assetBox.setSelectedItem(asset.getAssetId());
                }
                if (asset.hasMin()) {
                    minField.setText(String.valueOf(asset.getMin()));
                }
                if (asset.hasMax()) {
                    maxField.setText(String.valueOf(asset.getMax()));
                }
            }

            add(builder.getPanel());
        }

        @Override
        public Furniture createAsset() {
            Furniture.Builder builder = Furniture.newBuilder()
                    .setId((String) idBox.getSelectedItem())
                    .setType((Type) typeBox.getSelectedItem());
            if (assetBox.getItemCount() > 0) {
                builder.setAssetId((String) assetBox.getSelectedItem());
            }
            if (!minField.getText().isEmpty()) {
                builder.setMin(Integer.valueOf(minField.getText()));
            }
            if (!maxField.getText().isEmpty()) {
                builder.setMax(Integer.valueOf(maxField.getText()));
            }
            return builder.build();
        }

        private List<String> getFurnitureList() {
            String dir = MajorAssetTable.getTopAssetDirectory() + "/../furniture";
            File folder = new File(dir);
            File[] listOfFiles = folder.listFiles();

            List<String> list = new ArrayList<>();
            for (File file : listOfFiles) {
                if (file.isFile()) {
                    list.add(file.getName().substring(0, file.getName().indexOf(".")));
                }
            }
            return list;
        }

        private List<String> getAssets(Type type) {
            List<String> ids = new ArrayList<>();
            switch (type) {
                case CONTAINER:
                    ids.addAll(MainPanel.CONTAINER_TABLE.getAssetIds());
                    break;
                case TERMINAL:
                    ids.addAll(MainPanel.TERMINAL_TABLE.getAssetIds());
                    break;
                default:
            }
            return ids;
        }
    }
}
