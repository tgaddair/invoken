package com.eldritch.scifirpg.editor.tables;

import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.eldritch.invoken.proto.Actors.Container;
import com.eldritch.invoken.proto.Actors.InventoryItem;
import com.eldritch.scifirpg.editor.AssetTablePanel;
import com.eldritch.scifirpg.editor.panel.ActorEditorPanel.InventoryTable;
import com.eldritch.scifirpg.editor.panel.AssetEditorPanel;
import com.google.common.base.Optional;
import com.google.protobuf.TextFormat;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class ContainerTable extends MajorAssetTable<Container> {
    private static final long serialVersionUID = 1L;
    private static final String[] COLUMN_NAMES = { "ID", "Inventory" };

    public ContainerTable() {
        super(COLUMN_NAMES, "Container");
    }

    @Override
    protected JPanel getEditorPanel(Optional<Container> prev, JFrame frame) {
        return new EditorPanel(this, frame, prev);
    }

    @Override
    protected Object[] getDisplayFields(Container asset) {
        String inventory = asset.getItemCount() + "";
        return new Object[] { asset.getId(), inventory };
    }

    @Override
    protected String getAssetDirectory() {
        return "containers";
    }

    @Override
    protected Container readFromBinary(InputStream is) throws IOException {
        return Container.parseFrom(is);
    }

    @Override
    protected Container readFromText(InputStream is) throws IOException {
        Container.Builder builder = Container.newBuilder();
        TextFormat.merge(new InputStreamReader(is), builder);
        return builder.build();
    }

    @Override
    protected String getAssetId(Container asset) {
        return asset.getId();
    }

    private class EditorPanel extends AssetEditorPanel<Container, ContainerTable> {
        private static final long serialVersionUID = 1L;

        private final JTextField idField = new JTextField();
        private final InventoryTable itemTable = new InventoryTable();

        public EditorPanel(ContainerTable owner, JFrame frame, Optional<Container> prev) {
            super(owner, frame, prev);

            DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(""));
            builder.border(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            builder.appendColumn("right:pref");
            builder.appendColumn("3dlu");
            builder.appendColumn("fill:max(pref; 100px)");

            builder.append("ID:", idField);
            builder.nextLine();

            builder.appendRow("fill:p:grow");
            builder.append("Items:", new AssetTablePanel(itemTable));
            builder.nextLine();

            JButton saveButton = new JButton("Save");
            saveButton.addActionListener(this);
            builder.append(saveButton);
            builder.nextLine();

            if (prev.isPresent()) {
                Container asset = prev.get();
                idField.setText(asset.getId());
                for (InventoryItem item : asset.getItemList()) {
                    itemTable.addAsset(item);
                }
            }

            add(builder.getPanel());
            setPreferredSize(new Dimension(650, 750));
        }

        @Override
        public Container createAsset() {
            return Container.newBuilder().setId(idField.getText())
                    .addAllItem(itemTable.getAssets()).build();
        }
    }
}
