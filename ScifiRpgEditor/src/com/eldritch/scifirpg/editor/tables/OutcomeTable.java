package com.eldritch.scifirpg.editor.tables;

import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.eldritch.scifirpg.editor.panel.OutcomeEditorPanel;
import com.eldritch.invoken.proto.Outcomes.Outcome;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class OutcomeTable extends AssetTable<Outcome> {
	private static final long serialVersionUID = 1L;
	private static final String[] COLUMN_NAMES = { 
		"Type", "Target", "Value", "Weight" };
	
	public OutcomeTable() {
		super(COLUMN_NAMES, "Outcome");
	}
	
	public List<String> getEncounterIds() {
		return ImmutableList.of();
	}

	@Override
	protected JPanel getEditorPanel(Optional<Outcome> prev, JFrame frame) {
		return new OutcomeEditorPanel(this, frame, prev);
	}
	
	@Override
	protected Object[] getDisplayFields(Outcome asset) {
		Object target = asset.hasTarget() ? asset.getTarget() : "";
		Object count = asset.hasValue() ? asset.getValue() : "";
		return new Object[]{asset.getType(), target, count, asset.getWeight()};
	}
	
	public static class EncounterOutcomeTable extends OutcomeTable {
		private static final long serialVersionUID = 1L;
		private final EncounterTable table;
		
		public EncounterOutcomeTable(EncounterTable table) {
			this.table = table;
		}
		
		@Override
		public List<String> getEncounterIds() {
			return table.getAssetIds();
		}
	}
}
