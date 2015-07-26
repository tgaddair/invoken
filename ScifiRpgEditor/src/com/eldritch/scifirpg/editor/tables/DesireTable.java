package com.eldritch.scifirpg.editor.tables;

import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.eldritch.invoken.proto.Locations.DesireProto;
import com.eldritch.scifirpg.editor.panel.AssetEditorPanel;
import com.google.common.base.Optional;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class DesireTable extends AssetTable<DesireProto> {
    private static final long serialVersionUID = 1L;
    private static final String[] COLUMN_NAMES = { "Name" };

    public DesireTable() {
        super(COLUMN_NAMES, "Desire");
    }

    @Override
    protected JPanel getEditorPanel(Optional<DesireProto> prev, JFrame frame) {
        return new DesireEditorPanel(this, frame, prev);
    }

    @Override
    protected Object[] getDisplayFields(DesireProto asset) {
        return new Object[] { asset.name() };
    }

    public boolean contains(DesireProto aug) {
        for (DesireProto asset : getModel().getAssets()) {
            if (asset == aug) {
                return true;
            }
        }
        return false;
    }

    private static class DesireEditorPanel extends AssetEditorPanel<DesireProto, DesireTable> {
        private static final long serialVersionUID = 1L;

        private final JComboBox<DesireProto> comboBox = new JComboBox<>(DesireProto.values());

        public DesireEditorPanel(DesireTable owner, JFrame frame, Optional<DesireProto> prev) {
            super(owner, frame, prev);

            DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(""));
            builder.border(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            builder.appendColumn("right:pref");
            builder.appendColumn("3dlu");
            builder.appendColumn("fill:max(pref; 100px)");

            List<DesireProto> values = new ArrayList<>();
            for (DesireProto i : DesireProto.values()) {
                if ((prev.isPresent() && prev.get() == i) || !owner.contains(i)) {
                    values.add(i);
                }
            }
            comboBox.setModel(new DefaultComboBoxModel<DesireProto>(values
                    .toArray(new DesireProto[0])));
            builder.append("Desire:", comboBox);
            builder.nextLine();

            JButton saveButton = new JButton("Save");
            saveButton.addActionListener(this);
            builder.append(saveButton);
            builder.nextLine();

            if (prev.isPresent()) {
                DesireProto aug = prev.get();
                comboBox.setSelectedItem(aug);
            }

            add(builder.getPanel());
        }

        @Override
        public DesireProto createAsset() {
            return (DesireProto) comboBox.getSelectedItem();
        }
    }
}
