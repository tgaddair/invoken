package com.eldritch.scifirpg.editor.tables;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.eldritch.scifirpg.editor.panel.PrerequisiteEditorPanel;
import com.eldritch.invoken.proto.Prerequisites.Prerequisite;
import com.google.common.base.Optional;

public class PrerequisiteTable extends AssetTable<Prerequisite> {
	private static final long serialVersionUID = 1L;
	private static final String[] COLUMN_NAMES = { 
		"Not", "Type", "Target", "Min", "Max" };
	
	public PrerequisiteTable() {
		super(COLUMN_NAMES, "Prerequisite");
	}

	@Override
	protected JPanel getEditorPanel(Optional<Prerequisite> prev, JFrame frame) {
		return new PrerequisiteEditorPanel(this, frame, prev);
	}
	
	@Override
	protected Object[] getDisplayFields(Prerequisite asset) {
		Object not = asset.getNot() ? "not" : "";
		Object target = asset.hasTarget() ? asset.getTarget() : "";
		Object min = asset.hasMin() ? asset.getMin() : "";
		Object max = asset.hasMax() ? asset.getMax() : "";
		return new Object[]{not, asset.getType(), target, min, max};
	}
}
