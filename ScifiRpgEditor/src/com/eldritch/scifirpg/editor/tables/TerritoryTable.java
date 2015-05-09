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

import com.eldritch.scifirpg.editor.MainPanel;
import com.eldritch.scifirpg.editor.panel.AssetEditorPanel;
import com.eldritch.invoken.proto.Locations.Territory;
import com.google.common.base.Optional;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class TerritoryTable extends IdentifiedAssetTable<Territory> {
    private static final long serialVersionUID = 1L;
    private static final String[] COLUMN_NAMES = { "Faction", "Rank", "Credential", "Control" };

    public TerritoryTable() {
        super(COLUMN_NAMES, "Territory");
    }

    @Override
    protected JPanel getEditorPanel(Optional<Territory> prev, JFrame frame) {
        return new TerritoryEditorPanel(this, frame, prev);
    }

    @Override
    protected Object[] getDisplayFields(Territory asset) {
        return new Object[] { asset.getFactionId(), asset.getMinRank(), asset.getCredential(), asset.getControl() };
    }

    @Override
    protected String getAssetId(Territory asset) {
        return asset.getFactionId();
    }

    private static class TerritoryEditorPanel extends AssetEditorPanel<Territory, TerritoryTable> {
        private static final long serialVersionUID = 1L;

        private final JComboBox<String> factionBox = new JComboBox<>();
        private final JTextField minRankField = new JTextField();
        private final JComboBox<String> credentialBox = new JComboBox<>();
        private final JTextField controlField = new JTextField("0");
        private final JCheckBox compoundCheck = new JCheckBox();

        public TerritoryEditorPanel(TerritoryTable owner, JFrame frame, Optional<Territory> prev) {
            super(owner, frame, prev);

            DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(""));
            builder.border(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            builder.appendColumn("right:pref");
            builder.appendColumn("3dlu");
            builder.appendColumn("fill:max(pref; 100px)");

            List<String> fIds = new ArrayList<>();
            fIds.add("");
            fIds.addAll(MainPanel.FACTION_TABLE.getAssetIds());
            factionBox.setModel(new DefaultComboBoxModel<String>(fIds.toArray(new String[0])));
            builder.append("Faction:", factionBox);
            builder.nextLine();
            
            builder.append("Min Rank:", minRankField);
            builder.nextLine();
            
            List<String> items = new ArrayList<>();
            items.add("");
            items.addAll(MainPanel.ITEM_TABLE.getAssetIds());
            credentialBox.setModel(new DefaultComboBoxModel<String>(items.toArray(new String[0])));
            builder.append("Credential:", credentialBox);
            builder.nextLine();
            
            builder.append("Control:", controlField);
            builder.nextLine();
            
            builder.append("Compound:", compoundCheck);
            builder.nextLine();

            JButton saveButton = new JButton("Save");
            saveButton.addActionListener(this);
            builder.append(saveButton);
            builder.nextLine();

            if (prev.isPresent()) {
                Territory asset = prev.get();
                factionBox.setSelectedItem(asset.getFactionId());
                if (asset.hasMinRank()) {
                    minRankField.setText(asset.getMinRank() + "");
                }
                if (asset.hasCredential()) {
                    credentialBox.setSelectedItem(asset.getCredential());
                }
                controlField.setText(asset.getControl() + "");
                compoundCheck.setSelected(asset.getCompound());
            }

            add(builder.getPanel());
        }

        @Override
        public Territory createAsset() {
            Territory.Builder builder = Territory.newBuilder()
                    .setFactionId((String) factionBox.getSelectedItem());
            if (!minRankField.getText().isEmpty()) {
                builder.setMinRank(Integer.parseInt(minRankField.getText()));
            }
            builder.setCredential((String) credentialBox.getSelectedItem());
            builder.setControl(Integer.parseInt(controlField.getText()));
            builder.setCompound(compoundCheck.isSelected());
            return builder.build();
        }
    }
}
