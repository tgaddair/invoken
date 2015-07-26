package com.eldritch.scifirpg.editor.tables;

import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.eldritch.invoken.proto.Augmentations.AugmentationProto;
import com.eldritch.scifirpg.editor.panel.AssetEditorPanel;
import com.google.common.base.Optional;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class AugmentationTable extends AssetTable<AugmentationProto> {
    private static final long serialVersionUID = 1L;
    private static final String[] COLUMN_NAMES = { "Name" };

    public AugmentationTable() {
        super(COLUMN_NAMES, "Augmentation");
    }

    @Override
    protected JPanel getEditorPanel(Optional<AugmentationProto> prev, JFrame frame) {
        return new AugmentationEditorPanel(this, frame, prev);
    }

    @Override
    protected Object[] getDisplayFields(AugmentationProto asset) {
        return new Object[] { asset.name() };
    }

    public boolean contains(AugmentationProto aug) {
        for (AugmentationProto asset : getModel().getAssets()) {
            if (asset == aug) {
                return true;
            }
        }
        return false;
    }

    private static class AugmentationEditorPanel extends
            AssetEditorPanel<AugmentationProto, AugmentationTable> {
        private static final long serialVersionUID = 1L;

        private final JComboBox<AugmentationProto> comboBox = new JComboBox<>(
                AugmentationProto.values());

        public AugmentationEditorPanel(AugmentationTable owner, JFrame frame,
                Optional<AugmentationProto> prev) {
            super(owner, frame, prev);

            DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(""));
            builder.border(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            builder.appendColumn("right:pref");
            builder.appendColumn("3dlu");
            builder.appendColumn("fill:max(pref; 100px)");

            List<AugmentationProto> values = new ArrayList<>();
            for (AugmentationProto i : AugmentationProto.values()) {
                if ((prev.isPresent() && prev.get() == i) || !owner.contains(i)) {
                    values.add(i);
                }
            }
            comboBox.setModel(new DefaultComboBoxModel<AugmentationProto>(values
                    .toArray(new AugmentationProto[0])));
            builder.append("Augmentation:", comboBox);
            builder.nextLine();

            JButton saveButton = new JButton("Save");
            saveButton.addActionListener(this);
            builder.append(saveButton);
            builder.nextLine();

            if (prev.isPresent()) {
                AugmentationProto aug = prev.get();
                comboBox.setSelectedItem(aug);
            }

            add(builder.getPanel());
        }

        @Override
        public AugmentationProto createAsset() {
            return (AugmentationProto) comboBox.getSelectedItem();
        }
    }
}
