package com.eldritch.scifirpg.editor.tables;

import java.io.IOException;
import java.io.InputStream;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.eldritch.scifirpg.editor.asset.AugmentationEditorPanel;
import com.eldritch.scifirpg.proto.Augmentations.Augmentation;
import com.eldritch.scifirpg.proto.Augmentations.Augmentation.Requirement;
import com.google.common.base.Optional;

public class AugmentationTable extends MajorAssetTable<Augmentation> {
	private static final long serialVersionUID = 1L;
	private static final String[] COLUMN_NAMES = { 
		"ID", "Name", "Type", "Requirements", "Value"};
	
	public AugmentationTable() {
		super(COLUMN_NAMES);
	}

	@Override
	protected JPanel getEditorPanel(Optional<Augmentation> prev, JFrame frame) {
		return new AugmentationEditorPanel(this, frame, prev);
	}
	
	@Override
	protected String getAssetName() {
		return "Augmentation";
	}
	
	@Override
	protected String getAssetDirectory() {
		return "augmentations";
	}
	
	@Override
	protected String getAssetId(Augmentation asset) {
		return asset.getId();
	}
	
	@Override
	protected Augmentation readFrom(InputStream is) throws IOException {
		return Augmentation.parseFrom(is);
	}
	
	@Override
	protected Object[] getDisplayFields(Augmentation asset) {
		String reqs = "";
		boolean first = true;
		for (Requirement req : asset.getRequirementList()) {
			String discipline = req.getDiscipline().name().substring(0, 1);
			int slots = req.getSlots();
			int value = req.getValue();
			if (!first) {
				reqs += "; ";
			}
			reqs += String.format("%s: %d (%d)", discipline, value, slots);
			first = false;
		}
		return new Object[]{ asset.getId(), asset.getName(), asset.getType(), reqs, asset.getValue() };
	}
}
