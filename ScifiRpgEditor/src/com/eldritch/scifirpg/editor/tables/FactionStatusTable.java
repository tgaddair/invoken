package com.eldritch.scifirpg.editor.tables;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.eldritch.scifirpg.editor.panel.FactionStatusEditorPanel;
import com.eldritch.scifirpg.proto.Actors.ActorParams.FactionStatus;
import com.google.common.base.Optional;

public class FactionStatusTable extends AssetTable<FactionStatus> {
	private static final long serialVersionUID = 1L;
	private static final String[] COLUMN_NAMES = { 
		"Faction", "Reputation", "Rank" };
	
	public FactionStatusTable() {
		super(COLUMN_NAMES, "Faction Status");
	}

	@Override
	protected JPanel getEditorPanel(Optional<FactionStatus> prev, JFrame frame) {
		return new FactionStatusEditorPanel(this, frame, prev);
	}
	
	@Override
	protected Object[] getDisplayFields(FactionStatus status) {
		return new Object[]{status.getFactionId(), status.getReputation(), status.getRank()};
	}
}
