package com.eldritch.scifirpg.editor.tables;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.eldritch.scifirpg.editor.panel.OutcomeEditorPanel;
import com.eldritch.scifirpg.proto.Outcomes.Outcome;
import com.google.common.base.Optional;

public class OutcomeTable extends AssetTable<Outcome> {
	private static final long serialVersionUID = 1L;
	private static final String[] COLUMN_NAMES = { 
		"Type", "Target", "Count", "Weight" };
	
	public OutcomeTable() {
		super(COLUMN_NAMES, "Outcome");
	}

	@Override
	protected JPanel getEditorPanel(Optional<Outcome> prev, JFrame frame) {
		return new OutcomeEditorPanel(this, frame, prev);
	}
	
	@Override
	protected Object[] getDisplayFields(Outcome asset) {
		Object target = asset.hasTarget() ? asset.getTarget() : "";
		Object count = asset.hasCount() ? asset.getCount() : "";
		return new Object[]{asset.getType(), target, count, asset.getWeight()};
	}
}
