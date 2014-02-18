package com.eldritch.scifirpg.editor.tables;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.eldritch.scifirpg.editor.asset.RequirementEditorPanel;
import com.eldritch.scifirpg.proto.Augmentations.Augmentation.Requirement;
import com.google.common.base.Optional;

public class RequirementTable extends AssetTable<Requirement> {
	private static final long serialVersionUID = 1L;
	private static final String[] COLUMN_NAMES = { 
		"Discipline", "Value", "Slots" };
	
	public RequirementTable() {
		super(COLUMN_NAMES);
	}

	@Override
	protected JPanel getEditorPanel(Optional<Requirement> prev, JFrame frame) {
		return new RequirementEditorPanel(this, frame, prev);
	}
	
	@Override
	protected String getAssetName() {
		return "Requirement";
	}
	
	public void addAsset(Requirement req) {
		getModel().addAsset(req, new Object[]{req.getDiscipline(), req.getValue(), req.getSlots()});
	}
}
