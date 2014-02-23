package com.eldritch.scifirpg.editor.panel;

import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JTextField;

import com.eldritch.scifirpg.editor.AssetTablePanel;
import com.eldritch.scifirpg.editor.tables.EncounterTable;
import com.eldritch.scifirpg.editor.tables.PrerequisiteTable;
import com.eldritch.scifirpg.proto.Locations.Encounter;
import com.eldritch.scifirpg.proto.Locations.Encounter.Type;
import com.eldritch.scifirpg.proto.Prerequisites.Prerequisite;
import com.google.common.base.Optional;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class EncounterEditorPanel extends AssetEditorPanel<Encounter, EncounterTable> {
	private static final long serialVersionUID = 1L;

	private final JTextField idField = new JTextField();
	private final JTextField titleField = new JTextField();
	private final JComboBox<Type> typeBox = new JComboBox<Type>(Type.values());
	private final JTextField weightField = new JTextField();
	private final JCheckBox uniqueCheck = new JCheckBox();
	private final PrerequisiteTable prereqTable = new PrerequisiteTable();

	public EncounterEditorPanel(EncounterTable owner, JFrame frame, Optional<Encounter> prev) {
		super(owner, frame, prev);

		DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(""));
		builder.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		builder.appendColumn("right:pref");
		builder.appendColumn("3dlu");
		builder.appendColumn("fill:max(pref; 100px)");
		
		titleField.addActionListener(new NameTypedListener(idField));
		builder.append("Title:", titleField);
		builder.nextLine();
		
		builder.append("ID:", idField);
		builder.nextLine();
		
		builder.append("Type:", typeBox);
		builder.nextLine();
		
		builder.append("Weight:", weightField);
		builder.nextLine();
		
		builder.append("Unique:", uniqueCheck);
		builder.nextLine();
		
		builder.appendRow("fill:120dlu");
		builder.append("Prerequisites:", new AssetTablePanel(prereqTable));
		builder.nextLine();

		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(this);
		builder.append(saveButton);
		builder.nextLine();
		
		if (prev.isPresent()) {
			Encounter asset = prev.get();
			idField.setText(asset.getId());
			titleField.setText(asset.getTitle());
			weightField.setText(asset.getWeight() + "");
			uniqueCheck.setSelected(asset.getUnique());
			for (Prerequisite p : asset.getPrereqList()) {
				prereqTable.addAsset(p);
			}
		}

		add(builder.getPanel());
		setPreferredSize(new Dimension(650, 750));
	}

	@Override
	public Encounter createAsset() {
		// TODO Auto-generated method stub
		return null;
	}
}
