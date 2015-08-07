package com.eldritch.scifirpg.editor.tables;

import java.awt.Dimension;
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
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.eldritch.invoken.proto.Disciplines.Discipline;
import com.eldritch.invoken.proto.Effects.DamageType;
import com.eldritch.invoken.proto.Effects.Effect;
import com.eldritch.invoken.proto.Items.Item;
import com.eldritch.invoken.proto.Items.Item.DamageMod;
import com.eldritch.invoken.proto.Items.Item.RangedWeaponType;
import com.eldritch.invoken.proto.Items.Item.Requirement;
import com.eldritch.invoken.proto.Items.Item.Type;
import com.eldritch.scifirpg.editor.AssetTablePanel;
import com.eldritch.scifirpg.editor.panel.AssetEditorPanel;
import com.google.common.base.Optional;
import com.google.protobuf.TextFormat;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class ItemTable extends MajorAssetTable<Item> {
    private static final long serialVersionUID = 1L;
    private static final String[] COLUMN_NAMES = { "ID", "Name", "Type", "Value", "Effects",
            "Droppable", "Hidden" };

    public ItemTable() {
        super(COLUMN_NAMES, "Item");
    }

    @Override
    protected JPanel getEditorPanel(Optional<Item> prev, JFrame frame) {
        return new ItemEditorPanel(this, frame, prev);
    }

    @Override
    protected Object[] getDisplayFields(Item asset) {
        String effects = "";
        for (Effect e : asset.getEffectList()) {
            effects += e.getType() + " ";
        }
        Object droppable = asset.getDroppable() ? "yes" : "";
        Object hidden = asset.getHidden() ? "yes" : "";
        return new Object[] { asset.getId(), asset.getName(), asset.getType(), asset.getValue(),
                effects, droppable, hidden };
    }

    @Override
    protected String getAssetDirectory() {
        return "items";
    }

    @Override
    protected Item readFromBinary(InputStream is) throws IOException {
        return Item.parseFrom(is);
    }

    @Override
    protected Item readFromText(InputStream is) throws IOException {
        Item.Builder builder = Item.newBuilder();
        TextFormat.merge(new InputStreamReader(is), builder);
        return builder.build();
    }

    @Override
    protected String getAssetId(Item asset) {
        return asset.getId();
    }

    private class ItemEditorPanel extends AssetEditorPanel<Item, ItemTable> {
        private static final long serialVersionUID = 1L;

        private final JTextField idField = new JTextField();
        private final JTextField nameField = new JTextField();
        private final JComboBox<Type> typeBox = new JComboBox<>(Type.values());
        private final JTextArea descriptionField = createArea(true, 30, new Dimension(100, 100));
        private final JTextField valueField = new JTextField();
        private final EffectTable effectTable = new EffectTable();
        private final ItemRequirementTable requirementTable = new ItemRequirementTable();
        private final DamageModTable damageModTable = new DamageModTable();
        private final JCheckBox droppableCheck = new JCheckBox("", true);
        private final JCheckBox hiddenCheck = new JCheckBox();
        private final JCheckBox coversCheck = new JCheckBox();
        private final JTextField rangeField = new JTextField();
        private final JTextField cooldownField = new JTextField();
        private final JComboBox<RangedWeaponType> rangedTypeBox = new JComboBox<>(
                RangedWeaponType.values());
        private final JTextField assetField = new JTextField();

        public ItemEditorPanel(ItemTable owner, JFrame frame, Optional<Item> prev) {
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

            builder.append("Type:", typeBox);
            builder.nextLine();

            builder.append("Description:", descriptionField);
            builder.nextLine();

            builder.append("Value:", valueField);
            builder.nextLine();

            builder.append("Droppable:", droppableCheck);
            builder.nextLine();

            builder.append("Hidden:", hiddenCheck);
            builder.nextLine();

            builder.append("Covers:", coversCheck);
            builder.nextLine();

            builder.appendRow("fill:40dlu");
            builder.append("Effects:", new AssetTablePanel(effectTable));
            builder.nextLine();

            builder.appendRow("fill:40dlu");
            builder.append("Requirements:", new AssetTablePanel(requirementTable));
            builder.nextLine();

            builder.appendRow("fill:40dlu");
            builder.append("Damage Mods:", new AssetTablePanel(damageModTable));
            builder.nextLine();

            builder.append("Range:", rangeField);
            builder.nextLine();

            builder.append("Cooldown:", cooldownField);
            builder.nextLine();

            builder.append("Ranged Type:", rangedTypeBox);
            builder.nextLine();

            builder.append("Asset:", assetField);
            builder.nextLine();

            JButton saveButton = new JButton("Save");
            saveButton.addActionListener(this);
            builder.append(saveButton);
            builder.nextLine();

            if (prev.isPresent()) {
                Item item = prev.get();
                idField.setText(item.getId());
                nameField.setText(item.getName());
                typeBox.setSelectedItem(item.getType());
                descriptionField.setText(item.getDescription());
                valueField.setText(item.getValue() + "");
                droppableCheck.setSelected(item.getDroppable());
                hiddenCheck.setSelected(item.getHidden());
                coversCheck.setSelected(item.getCovers());
                for (Effect asset : item.getEffectList()) {
                    effectTable.addAsset(asset);
                }
                for (Requirement asset : item.getRequirementList()) {
                    requirementTable.addAsset(asset);
                }
                for (DamageMod asset : item.getDamageModifierList()) {
                    damageModTable.addAsset(asset);
                }
                if (item.hasRange()) {
                    rangeField.setText(item.getRange() + "");
                }
                if (item.hasCooldown()) {
                    cooldownField.setText(item.getCooldown() + "");
                }
                if (item.hasRangedType()) {
                    rangedTypeBox.setSelectedItem(item.getRangedType());
                }
                if (item.hasAsset()) {
                    assetField.setText(item.getAsset());
                }
            }

            add(builder.getPanel());
        }

        @Override
        public Item createAsset() {
            Item.Builder item = Item.newBuilder().setId(idField.getText())
                    .setName(nameField.getText()).setDescription(descriptionField.getText())
                    .setValue(Integer.parseInt(valueField.getText()))
                    .setType((Type) typeBox.getSelectedItem())
                    .setDroppable(droppableCheck.isSelected()).setHidden(hiddenCheck.isSelected())
                    .setCovers(coversCheck.isSelected()).addAllEffect(effectTable.getAssets())
                    .addAllRequirement(requirementTable.getAssets())
                    .addAllDamageModifier(damageModTable.getAssets())
                    .setAsset(assetField.getText());
            if (!rangeField.getText().isEmpty()) {
                item.setRange(Double.parseDouble(rangeField.getText()));
            }
            if (!cooldownField.getText().isEmpty()) {
                item.setCooldown(Double.parseDouble(cooldownField.getText()));
            }
            if (item.getType() == Type.RANGED_WEAPON || item.getType() == Type.AMMUNITION) {
                item.setRangedType((RangedWeaponType) rangedTypeBox.getSelectedItem());
            }
            return item.build();
        }
    }

    private static class ItemRequirementTable extends AssetTable<Requirement> {
        private static final long serialVersionUID = 1L;
        private static final String[] COLUMN_NAMES = { "Discipline", "Value" };

        public ItemRequirementTable() {
            super(COLUMN_NAMES, "Requirement");
        }

        @Override
        protected JPanel getEditorPanel(Optional<Requirement> prev, JFrame frame) {
            return new ItemRequirementEditorPanel(this, frame, prev);
        }

        @Override
        protected Object[] getDisplayFields(Requirement req) {
            return new Object[] { req.getDiscipline(), req.getValue() };
        }

        public boolean containsDiscipline(Discipline d) {
            for (Requirement r : getModel().getAssets()) {
                if (r.getDiscipline() == d) {
                    return true;
                }
            }
            return false;
        }
    }

    private static class ItemRequirementEditorPanel extends
            AssetEditorPanel<Requirement, ItemRequirementTable> {
        private static final long serialVersionUID = 1L;

        private final JComboBox<Discipline> disciplineBox = new JComboBox<>(Discipline.values());
        private final JTextField valueField = new JTextField();

        public ItemRequirementEditorPanel(ItemRequirementTable owner, JFrame frame,
                Optional<Requirement> prev) {
            super(owner, frame, prev);

            DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(""));
            builder.border(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            builder.appendColumn("right:pref");
            builder.appendColumn("3dlu");
            builder.appendColumn("fill:max(pref; 100px)");

            List<Discipline> values = new ArrayList<>();
            for (Discipline d : Discipline.values()) {
                if ((prev.isPresent() && prev.get().getDiscipline() == d)
                        || !owner.containsDiscipline(d)) {
                    values.add(d);
                }
            }
            disciplineBox.setModel(new DefaultComboBoxModel<Discipline>(values
                    .toArray(new Discipline[0])));
            builder.append("Discipline:", disciplineBox);
            builder.nextLine();

            builder.append("Value:", valueField);
            builder.nextLine();

            JButton saveButton = new JButton("Save");
            saveButton.addActionListener(this);
            builder.append(saveButton);
            builder.nextLine();

            if (prev.isPresent()) {
                Requirement req = prev.get();
                disciplineBox.setSelectedItem(req.getDiscipline());
                valueField.setText(req.getValue() + "");
            }

            add(builder.getPanel());
        }

        @Override
        public Requirement createAsset() {
            Discipline discipline = (Discipline) disciplineBox.getSelectedItem();
            int value = Integer.parseInt(valueField.getText());
            Requirement req = Requirement.newBuilder().setDiscipline(discipline).setValue(value)
                    .build();
            return req;
        }
    }

    private static class DamageModTable extends AssetTable<DamageMod> {
        private static final long serialVersionUID = 1L;
        private static final String[] COLUMN_NAMES = { "Damage", "Magnitude" };

        public DamageModTable() {
            super(COLUMN_NAMES, "Damage Modifier");
        }

        @Override
        protected JPanel getEditorPanel(Optional<DamageMod> prev, JFrame frame) {
            return new DamageModEditorPanel(this, frame, prev);
        }

        @Override
        protected Object[] getDisplayFields(DamageMod asset) {
            return new Object[] { asset.getDamage(), asset.getMagnitude() };
        }

        public boolean containsDamage(DamageType t) {
            for (DamageMod mod : getModel().getAssets()) {
                if (mod.getDamage() == t) {
                    return true;
                }
            }
            return false;
        }
    }

    private static class DamageModEditorPanel extends AssetEditorPanel<DamageMod, DamageModTable> {
        private static final long serialVersionUID = 1L;

        private final JComboBox<DamageType> damageBox = new JComboBox<>(DamageType.values());
        private final JTextField magnitudeField = new JTextField();

        public DamageModEditorPanel(DamageModTable owner, JFrame frame, Optional<DamageMod> prev) {
            super(owner, frame, prev);

            DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(""));
            builder.border(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            builder.appendColumn("right:pref");
            builder.appendColumn("3dlu");
            builder.appendColumn("fill:max(pref; 100px)");

            List<DamageType> values = new ArrayList<>();
            for (DamageType t : DamageType.values()) {
                if ((prev.isPresent() && prev.get().getDamage() == t) || !owner.containsDamage(t)) {
                    values.add(t);
                }
            }
            damageBox.setModel(new DefaultComboBoxModel<DamageType>(values
                    .toArray(new DamageType[0])));
            builder.append("Damage:", damageBox);
            builder.nextLine();

            builder.append("Magnitude:", magnitudeField);
            builder.nextLine();

            JButton saveButton = new JButton("Save");
            saveButton.addActionListener(this);
            builder.append(saveButton);
            builder.nextLine();

            if (prev.isPresent()) {
                DamageMod mod = prev.get();
                damageBox.setSelectedItem(mod.getDamage());
                magnitudeField.setText(mod.getMagnitude() + "");
            }

            add(builder.getPanel());
        }

        @Override
        public DamageMod createAsset() {
            DamageType damage = (DamageType) damageBox.getSelectedItem();
            int value = Integer.parseInt(magnitudeField.getText());
            return DamageMod.newBuilder().setDamage(damage).setMagnitude(value).build();
        }
    }
}
