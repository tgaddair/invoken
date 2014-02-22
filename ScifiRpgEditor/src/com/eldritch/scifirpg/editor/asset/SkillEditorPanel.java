package com.eldritch.scifirpg.editor.asset;

import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JTextField;

import com.eldritch.scifirpg.editor.tables.SkillTable;
import com.eldritch.scifirpg.proto.Actors.ActorParams.Skill;
import com.eldritch.scifirpg.proto.Disciplines.Discipline;
import com.google.common.base.Optional;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class SkillEditorPanel extends AssetEditorPanel<Skill, SkillTable> {
	private static final long serialVersionUID = 1L;

	private final JComboBox<Discipline> disciplineBox = new JComboBox<>(Discipline.values());
	private final JTextField levelField = new JTextField();

	public SkillEditorPanel(SkillTable owner, JFrame frame, Optional<Skill> prev) {
		super(owner, frame, prev);

		DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(""));
		builder.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		builder.appendColumn("right:pref");
		builder.appendColumn("3dlu");
		builder.appendColumn("fill:max(pref; 100px)");
		
		List<Discipline> values = new ArrayList<>();
		for (Discipline d : Discipline.values()) {
			if ((prev.isPresent() && prev.get().getDiscipline() == d) || !owner.containsDiscipline(d)) {
				values.add(d);
			}
		}
		disciplineBox.setModel(new DefaultComboBoxModel<Discipline>(values.toArray(new Discipline[0])));
		builder.append("Discipline:", disciplineBox);
		builder.nextLine();

		builder.append("Level:", levelField);
		builder.nextLine();

		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(this);
		builder.append(saveButton);
		builder.nextLine();
		
		if (prev.isPresent()) {
			Skill skill = prev.get();
			disciplineBox.setSelectedItem(skill.getDiscipline());
			levelField.setText(skill.getLevel() + "");
		}

		add(builder.getPanel());
	}

	@Override
	public Skill createAsset() {
		Discipline discipline = (Discipline) disciplineBox.getSelectedItem();
		int level = Integer.parseInt(levelField.getText());
		Skill req = Skill.newBuilder().setDiscipline(discipline)
				.setLevel(level)
				.build();
		return req;
	}
}
