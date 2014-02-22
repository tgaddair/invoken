package com.eldritch.scifirpg.editor.tables;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.eldritch.scifirpg.editor.panel.FactionEditorPanel;
import com.eldritch.scifirpg.editor.panel.RelationEditorPanel;
import com.eldritch.scifirpg.proto.Factions.Faction.Relation;
import com.google.common.base.Optional;

public class RelationTable extends AssetTable<Relation> {
	private static final long serialVersionUID = 1L;
	private static final String[] COLUMN_NAMES = { 
		"Faction", "Reaction" };
	
	private final FactionEditorPanel owner;
	
	public RelationTable(FactionEditorPanel owner) {
		super(COLUMN_NAMES, "Relation");
		this.owner = owner;
	}
	
	public String getCurrentFactionId() {
		return owner.getCurrentFactionId();
	}

	@Override
	protected JPanel getEditorPanel(Optional<Relation> prev, JFrame frame) {
		return new RelationEditorPanel(this, frame, prev);
	}
	
	@Override
	protected Object[] getDisplayFields(Relation r) {
		return new Object[]{r.getFactionId(), r.getReaction()};
	}
}
