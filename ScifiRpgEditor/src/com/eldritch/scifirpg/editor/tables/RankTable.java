package com.eldritch.scifirpg.editor.tables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.eldritch.scifirpg.editor.panel.RankEditorPanel;
import com.eldritch.scifirpg.proto.Factions.Faction.Rank;
import com.google.common.base.Optional;

public class RankTable extends AssetTable<Rank> {
	private static final long serialVersionUID = 1L;
	private static final String[] COLUMN_NAMES = { 
		"ID", "Title" };
	
	public RankTable() {
		super(COLUMN_NAMES, "Rank");
	}

	@Override
	protected JPanel getEditorPanel(Optional<Rank> prev, JFrame frame) {
		return new RankEditorPanel(this, frame, prev);
	}
	
	@Override
	protected Object[] getDisplayFields(Rank r) {
		return new Object[]{r.getId(), r.getTitle()};
	}
	
	public List<Rank> getSortedAssets() {
		List<Rank> ranks = new ArrayList<>(getAssets());
		Collections.sort(ranks, new Comparator<Rank>() {
			@Override
			public int compare(Rank r1, Rank r2) {
				return Integer.compare(r1.getId(), r2.getId());
			}
		});
		return ranks;
	}
}
