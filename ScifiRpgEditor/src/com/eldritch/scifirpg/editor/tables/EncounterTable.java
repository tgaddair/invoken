package com.eldritch.scifirpg.editor.tables;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.eldritch.scifirpg.editor.panel.EncounterEditorPanel;
import com.eldritch.scifirpg.proto.Locations.Encounter;
import com.google.common.base.Optional;

public class EncounterTable extends AssetTable<Encounter> {
	private static final long serialVersionUID = 1L;
	private static final String[] COLUMN_NAMES = { 
		"ID", "Title", "Type", "Weight", "Unique" };
	
	public EncounterTable() {
		super(COLUMN_NAMES, "Encounter");
	}

	@Override
	protected JPanel getEditorPanel(Optional<Encounter> prev, JFrame frame) {
		return new EncounterEditorPanel(this, frame, prev);
	}
	
	@Override
	protected Object[] getDisplayFields(Encounter asset) {
		return new Object[]{asset.getId(), asset.getTitle(), asset.getType(),
				asset.getWeight(), asset.getUnique()};
	}
}
