package com.eldritch.scifirpg.editor.tables;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;

import com.eldritch.scifirpg.editor.asset.RequirementEditorPanel;
import com.eldritch.scifirpg.proto.Augmentations.Augmentation.Requirement;

public class RequirementTable extends AssetTable {
	private static final long serialVersionUID = 1L;
	private static final String[] COLUMN_NAMES = { 
		"Discipline", "Value", "Slots" };
	
	public RequirementTable() {
		super(COLUMN_NAMES);
	}

	@Override
	protected JPanel getEditorPanel(JFrame frame) {
		return new RequirementEditorPanel(this, frame);
	}
	
	@Override
	protected String getAssetName() {
		return "Requirement";
	}
	
	public void addAsset(Requirement req) {
		DefaultTableModel model = (DefaultTableModel) getModel();
		model.addRow(new Object[]{req.getDiscipline(), req.getValue(), req.getSlots()});
	}
}
