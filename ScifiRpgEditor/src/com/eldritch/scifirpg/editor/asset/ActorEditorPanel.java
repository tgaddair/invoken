package com.eldritch.scifirpg.editor.asset;

import java.awt.Dimension;

import javax.swing.*;

import com.eldritch.scifirpg.editor.AssetTablePanel;
import com.eldritch.scifirpg.editor.tables.ActorTable;
import com.eldritch.scifirpg.editor.tables.EffectTable;
import com.eldritch.scifirpg.editor.tables.RequirementTable;
import com.eldritch.scifirpg.proto.Actors.NonPlayerActor;
import com.eldritch.scifirpg.proto.Augmentations.Augmentation.Type;
import com.google.common.base.Optional;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class ActorEditorPanel extends AssetEditorPanel<NonPlayerActor, ActorTable> {
	private static final long serialVersionUID = 1L;
	
	private final JTextField idField = new JTextField();
	private final JTextField nameField = new JTextField();
	private final JTextArea descriptionField = createArea(true, 30, new Dimension(100, 100));
	private final JTextField valueField = new JTextField();
	private final JComboBox<Type> typeBox = new JComboBox<Type>(Type.values());
	//private final JComboBox<Enum<?>> subtypeBox = new JComboBox<Enum<?>>(AttackSubtype.values());
	private final EffectTable effectTable = new EffectTable();
	private final RequirementTable requirementTable = new RequirementTable();

	public ActorEditorPanel(ActorTable owner, JFrame frame, Optional<NonPlayerActor> prev) {
		super(owner, frame, prev);
		
		FormLayout layout = new FormLayout(
				"right:p, 4dlu, p, 7dlu, right:p, 4dlu, p, 4dlu, p", // columns
				"p, 3dlu, p, 3dlu, fill:default:grow, 3dlu, p, 3dlu, p"); // rows
		
		// Specify that columns 1 & 5 as well as 3 & 7 have equal widths.       
		layout.setColumnGroups(new int[][]{{1, 5}, {3, 7}});

		PanelBuilder builder = new PanelBuilder(layout);
		builder.setDefaultDialogBorder();
		CellConstraints cc = new CellConstraints();
		int r = 1;
		int c = 1;
		
		nameField.addActionListener(new NameTypedListener(idField));
		builder.addLabel("Name", cc.xy(c, r));
		builder.add(nameField, cc.xy(c + 2, r));
		r += 2;
		
		builder.addLabel("ID", cc.xy(c, r));
		builder.add(idField, cc.xy(c + 2, r));
		r += 2;
		
		builder.addLabel("Description", cc.xy(c, r));
		builder.add(descriptionField, cc.xy(c + 2, r));
		r += 2;
		
		c = 5;
		r = 1;
		
		builder.addLabel("Value", cc.xy(c, r));
		builder.add(valueField, cc.xy(c + 2, r));
		r += 2;
		
		builder.addLabel("Type", cc.xy(c, r));
		builder.add(typeBox, cc.xy(c + 2, r));
		r += 2;

		
		builder.addLabel("Effects", cc.xy(c, r));
		builder.add(new AssetTablePanel(effectTable), cc.xy(c + 2, r));
		r += 2;
		
		builder.addLabel("Requirements", cc.xy(c, r));
		builder.add(new AssetTablePanel(requirementTable), cc.xy(c + 2, r));
		r += 2;
		
		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(this);
		builder.add(saveButton, cc.xy(c + 4, 9));
		
		if (prev.isPresent()) {
			/*
			Augmentation asset = prev.get();
			idField.setText(asset.getId());
			nameField.setText(asset.getName());
			descriptionField.setText(asset.getDescription());
			valueField.setText(asset.getValue() + "");
			typeBox.setSelectedItem(asset.getType());
			for (Effect effect : asset.getEffectList()) {
				effectTable.addAsset(effect);
			}
			for (Requirement req : asset.getRequirementList()) {
				requirementTable.addAsset(req);
			}
			*/
		}

		add(builder.getPanel());
		setPreferredSize(new Dimension(1400, 500));
	}

	@Override
	public NonPlayerActor createAsset() {
		// TODO Auto-generated method stub
		return null;
	}
}
