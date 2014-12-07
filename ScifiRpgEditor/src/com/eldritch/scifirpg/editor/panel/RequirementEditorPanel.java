package com.eldritch.scifirpg.editor.panel;

import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JTextField;

import com.eldritch.scifirpg.editor.tables.RequirementTable;
import com.eldritch.invoken.proto.Augmentations.Augmentation.Requirement;
import com.eldritch.invoken.proto.Disciplines.Discipline;
import com.google.common.base.Optional;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class RequirementEditorPanel extends AssetEditorPanel<Requirement, RequirementTable> {
	private static final long serialVersionUID = 1L;

	private final JComboBox<Discipline> disciplineBox = new JComboBox<>(Discipline.values());
	private final JTextField valueField = new JTextField();
	private final JTextField slotsField = new JTextField();

	public RequirementEditorPanel(RequirementTable owner, JFrame frame, Optional<Requirement> prev) {
		super(owner, frame, prev);

		DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(""));
		builder.border(BorderFactory.createEmptyBorder(5, 5, 5, 5));
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

		builder.append("Value:", valueField);
		builder.nextLine();

		builder.append("Slots:", slotsField);
		builder.nextLine();

		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(this);
		builder.append(saveButton);
		builder.nextLine();
		
		if (prev.isPresent()) {
			Requirement req = prev.get();
			disciplineBox.setSelectedItem(req.getDiscipline());
			valueField.setText(req.getValue() + "");
			slotsField.setText(req.getSlots() + "");
		}

		add(builder.getPanel());
	}

	@Override
	public Requirement createAsset() {
		Discipline discipline = (Discipline) disciplineBox.getSelectedItem();
		int value = Integer.parseInt(valueField.getText());
		int slots = Integer.parseInt(slotsField.getText());
		Requirement req = Requirement.newBuilder().setDiscipline(discipline)
				.setValue(value)
				.setSlots(slots)
				.build();
		return req;
	}
}
