package com.eldritch.scifirpg.editor.asset;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.eldritch.scifirpg.editor.tables.RequirementTable;
import com.eldritch.scifirpg.proto.Augmentations.Augmentation.Requirement;
import com.eldritch.scifirpg.proto.Disciplines.Discipline;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class RequirementEditorPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;

	private final RequirementTable owner;
	private final JFrame frame;
	private final JComboBox<Discipline> disciplineBox = new JComboBox<Discipline>(
			Discipline.values());
	private final JTextField valueField = new JTextField();
	private final JTextField slotsField = new JTextField();

	public RequirementEditorPanel(RequirementTable owner, JFrame frame) {
		super(new BorderLayout());
		this.owner = owner;
		this.frame = frame;

		DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(""));
		builder.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		builder.appendColumn("right:pref");
		builder.appendColumn("3dlu");
		builder.appendColumn("fill:max(pref; 100px)");

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

		add(builder.getPanel());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Discipline discipline = (Discipline) disciplineBox.getSelectedItem();
		int value = Integer.parseInt(valueField.getText());
		int slots = Integer.parseInt(slotsField.getText());
		Requirement req = Requirement.newBuilder().setDiscipline(discipline)
				.setValue(value)
				.setSlots(slots)
				.build();
		owner.addAsset(req);
		frame.dispose();
	}
}
