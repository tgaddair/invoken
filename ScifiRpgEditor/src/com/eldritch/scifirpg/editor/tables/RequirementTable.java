package com.eldritch.scifirpg.editor.tables;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.eldritch.scifirpg.editor.panel.RequirementEditorPanel;
import com.eldritch.invoken.proto.Augmentations.Augmentation.Requirement;
import com.eldritch.invoken.proto.Disciplines.Discipline;
import com.google.common.base.Optional;

public class RequirementTable extends AssetTable<Requirement> {
	private static final long serialVersionUID = 1L;
	private static final String[] COLUMN_NAMES = { 
		"Discipline", "Value", "Slots" };
	
	public RequirementTable() {
		super(COLUMN_NAMES, "Requirement");
	}

	@Override
	protected JPanel getEditorPanel(Optional<Requirement> prev, JFrame frame) {
		return new RequirementEditorPanel(this, frame, prev);
	}
	
	@Override
	protected Object[] getDisplayFields(Requirement req) {
		return new Object[]{req.getDiscipline(), req.getValue(), req.getSlots()};
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
