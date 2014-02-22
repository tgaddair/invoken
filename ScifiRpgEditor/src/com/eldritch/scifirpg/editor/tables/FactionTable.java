package com.eldritch.scifirpg.editor.tables;

import java.io.IOException;
import java.io.InputStream;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.eldritch.scifirpg.editor.panel.FactionEditorPanel;
import com.eldritch.scifirpg.proto.Factions.Faction;
import com.google.common.base.Optional;

public class FactionTable extends MajorAssetTable<Faction> {
	private static final long serialVersionUID = 1L;
	private static final String[] COLUMN_NAMES = { 
		"ID", "Name", "Alignment" };
	
	public FactionTable() {
		super(COLUMN_NAMES, "Faction");
	}

	@Override
	protected JPanel getEditorPanel(Optional<Faction> prev, JFrame frame) {
		return new FactionEditorPanel(this, frame, prev);
	}

	@Override
	protected Object[] getDisplayFields(Faction asset) {
		Object alignment = asset.hasAlignment() ? asset.getAlignment() : "";
		return new Object[]{asset.getId(), asset.getName(), alignment};
	}

	@Override
	protected String getAssetDirectory() {
		return "factions";
	}

	@Override
	protected String getAssetId(Faction asset) {
		return asset.getId();
	}

	@Override
	protected Faction readFrom(InputStream is) throws IOException {
		return Faction.parseFrom(is);
	}
}
