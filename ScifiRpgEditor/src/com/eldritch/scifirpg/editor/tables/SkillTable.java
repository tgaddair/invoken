package com.eldritch.scifirpg.editor.tables;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.eldritch.scifirpg.editor.asset.SkillEditorPanel;
import com.eldritch.scifirpg.proto.Actors.ActorParams.Skill;
import com.eldritch.scifirpg.proto.Disciplines.Discipline;
import com.google.common.base.Optional;

public class SkillTable extends AssetTable<Skill> {
	private static final long serialVersionUID = 1L;
	private static final String[] COLUMN_NAMES = { 
		"Discipline", "Level" };
	
	public SkillTable() {
		super(COLUMN_NAMES);
	}

	@Override
	protected JPanel getEditorPanel(Optional<Skill> prev, JFrame frame) {
		return new SkillEditorPanel(this, frame, prev);
	}
	
	@Override
	protected String getAssetName() {
		return "Requirement";
	}
	
	@Override
	protected Object[] getDisplayFields(Skill skill) {
		return new Object[]{skill.getDiscipline(), skill.getLevel()};
	}
	
	public boolean containsDiscipline(Discipline d) {
		for (Skill asset : getModel().getAssets()) {
			if (asset.getDiscipline() == d) {
				return true;
			}
		}
		return false;
	}
}
