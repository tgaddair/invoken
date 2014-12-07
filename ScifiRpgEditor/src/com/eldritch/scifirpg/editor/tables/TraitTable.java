package com.eldritch.scifirpg.editor.tables;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.eldritch.scifirpg.editor.panel.TraitEditorPanel;
import com.eldritch.invoken.proto.Actors.NonPlayerActor.Trait;
import com.eldritch.invoken.proto.Disciplines.Influence;
import com.google.common.base.Optional;

public class TraitTable extends AssetTable<Trait> {
	private static final long serialVersionUID = 1L;
	private static final String[] COLUMN_NAMES = { 
		"Influence", "Effective" };
	
	public TraitTable() {
		super(COLUMN_NAMES, "Trait");
	}

	@Override
	protected JPanel getEditorPanel(Optional<Trait> prev, JFrame frame) {
		return new TraitEditorPanel(this, frame, prev);
	}
	
	@Override
	protected Object[] getDisplayFields(Trait asset) {
		return new Object[]{asset.getInfluence(), asset.getEffective()};
	}
	
	public boolean containsInfluence(Influence i) {
		for (Trait asset : getModel().getAssets()) {
			if (asset.getInfluence() == i) {
				return true;
			}
		}
		return false;
	}
}
