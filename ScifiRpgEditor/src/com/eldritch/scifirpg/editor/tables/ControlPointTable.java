package com.eldritch.scifirpg.editor.tables;

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

import com.eldritch.scifirpg.editor.AssetTablePanel;
import com.eldritch.scifirpg.editor.MainPanel;
import com.eldritch.scifirpg.editor.panel.AssetEditorPanel;
import com.eldritch.invoken.proto.Items.Item;
import com.eldritch.invoken.proto.Locations.ControlPoint;
import com.eldritch.invoken.proto.Locations.Room;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class ControlPointTable extends IdentifiedAssetTable<ControlPoint> {
    private static final long serialVersionUID = 1L;
    private static final String[] COLUMN_NAMES = { "ID", "Value", "Rooms", "Follows" };

    public ControlPointTable() {
        super(COLUMN_NAMES, "ControlPoint");
    }

    @Override
    protected JPanel getEditorPanel(Optional<ControlPoint> prev, JFrame frame) {
        return new ControlPointEditorPanel(this, frame, prev);
    }

    @Override
    protected Object[] getDisplayFields(ControlPoint asset) {
        String rooms = Joiner.on(" ").join(asset.getRoomIdList());
        String follows = Joiner.on(" ").join(asset.getFollowsList());
        return new Object[] { asset.getId(), asset.getValue(), rooms, follows };
    }

    @Override
    protected String getAssetId(ControlPoint asset) {
        return asset.getId();
    }

    private static class ControlPointEditorPanel extends
            AssetEditorPanel<ControlPoint, ControlPointTable> {
        private static final long serialVersionUID = 1L;

        private final JTextField idField = new JTextField();
        private final JTextField valueField = new JTextField();

        private final AssetPointerTable<Room> roomTable = new AssetPointerTable<>(
                MainPanel.ROOM_TABLE);

        private final JTextField lockField = new JTextField();
        private final JCheckBox originCheck = new JCheckBox();
        private final JComboBox<String> lockBox = new JComboBox<>();
        private final AssetPointerTable<Item> keyTable = new AssetPointerTable<>(
                MainPanel.ITEM_TABLE);

        public ControlPointEditorPanel(ControlPointTable owner, JFrame frame,
                Optional<ControlPoint> prev) {
            super(owner, frame, prev);

            DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(""));
            builder.border(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            builder.appendColumn("right:pref");
            builder.appendColumn("3dlu");
            builder.appendColumn("fill:max(pref; 100px)");

            builder.append("ID:", idField);
            builder.nextLine();

            builder.append("Value:", valueField);
            builder.nextLine();

            builder.appendRow("fill:100dlu");
            builder.append("Rooms:", new AssetTablePanel(roomTable));
            builder.nextLine();

            builder.append("Origin:", originCheck);
            builder.nextLine();

            lockField.setText("0");
            builder.append("Lock:", lockField);
            builder.nextLine();

            List<String> items = new ArrayList<>();
            items.add("");
            items.addAll(MainPanel.ITEM_TABLE.getAssetIds());
            lockBox.setModel(new DefaultComboBoxModel<String>(items.toArray(new String[0])));
            builder.append("Key:", lockBox);
            builder.nextLine();

            builder.appendRow("fill:100dlu");
            builder.append("Unlocks:", new AssetTablePanel(keyTable));
            builder.nextLine();

            JButton saveButton = new JButton("Save");
            saveButton.addActionListener(this);
            builder.append(saveButton);
            builder.nextLine();

            if (prev.isPresent()) {
                ControlPoint asset = prev.get();
                idField.setText(asset.getId());
                valueField.setText(asset.getValue() + "");
                for (String roomId : asset.getRoomIdList()) {
                    roomTable.addAssetId(roomId);
                }

                originCheck.setSelected(asset.getOrigin());
                lockField.setText(asset.getLockStrength() + "");
                if (asset.hasRequiredKey()) {
                    lockBox.setSelectedItem(asset.getRequiredKey());
                }
                for (String key : asset.getAvailableKeyList()) {
                    keyTable.addAssetId(key);
                }
            }

            add(builder.getPanel());
        }

        @Override
        public ControlPoint createAsset() {
            ControlPoint.Builder builder = ControlPoint.newBuilder()
                    .setId(idField.getText())
                    .setValue(Integer.parseInt(valueField.getText()))
                    .setOrigin(originCheck.isSelected())
                    .setLockStrength(Integer.parseInt(lockField.getText()))
                    .setRequiredKey((String) lockBox.getSelectedItem())
                    .addAllAvailableKey(keyTable.getAssetIds())
                    .addAllRoomId(roomTable.getAssetIds());
            return builder.build();
        }
    }
}
