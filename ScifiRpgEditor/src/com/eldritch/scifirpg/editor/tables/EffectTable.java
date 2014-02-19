package com.eldritch.scifirpg.editor.tables;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.eldritch.scifirpg.editor.asset.EffectEditorPanel;
import com.eldritch.scifirpg.proto.Effects.Effect;
import com.google.common.base.Optional;

public class EffectTable extends AssetTable<Effect> {
	private static final long serialVersionUID = 1L;
	private static final String[] COLUMN_NAMES = { 
		"Type", "Range", "Magnitude", "Duration", "Target" };
	
	public EffectTable() {
		super(COLUMN_NAMES);
	}

	@Override
	protected JPanel getEditorPanel(Optional<Effect> prev, JFrame frame) {
		return new EffectEditorPanel(this, frame, prev);
	}
	
	@Override
	protected String getAssetName() {
		return "Effect";
	}
}
