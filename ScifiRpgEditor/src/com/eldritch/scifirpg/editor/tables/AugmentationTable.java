package com.eldritch.scifirpg.editor.tables;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.eldritch.scifirpg.editor.asset.AugmentationEditorPanel;
import com.eldritch.scifirpg.proto.Augmentations.Augmentation;
import com.google.common.base.Optional;

public class AugmentationTable extends AssetTable<Augmentation> {
	private static final long serialVersionUID = 1L;
	private static final String[] COLUMN_NAMES = { 
		"ID", "Name", "Value", "Slots", "Discipline" };
	
	public AugmentationTable() {
		super(COLUMN_NAMES);
	}

	@Override
	protected JPanel getEditorPanel(Optional<Augmentation> prev, JFrame frame) {
		return new AugmentationEditorPanel();
	}
	
	@Override
	protected String getAssetName() {
		return "Augmentation";
	}
}
