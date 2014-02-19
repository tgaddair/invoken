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

import com.eldritch.scifirpg.editor.tables.EffectTable;
import com.eldritch.scifirpg.proto.Effects.Effect;
import com.eldritch.scifirpg.proto.Effects.Effect.Range;
import com.eldritch.scifirpg.proto.Effects.Effect.Type;
import com.google.common.base.Optional;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class EffectEditorPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;

	private final EffectTable owner;
	private final JFrame frame;
	private final Optional<Effect> prev;
	private final JComboBox<Type> typeBox = new JComboBox<>(Type.values());
	private final JComboBox<Range> rangeBox = new JComboBox<>(Range.values());
	private final JTextField magnitudeField = new JTextField();
	private final JTextField durationField = new JTextField("0");
	private final JTextField targetField = new JTextField();

	public EffectEditorPanel(EffectTable owner, JFrame frame, Optional<Effect> prev) {
		super(new BorderLayout());
		this.owner = owner;
		this.frame = frame;
		this.prev = prev;

		DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(""));
		builder.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		builder.appendColumn("right:pref");
		builder.appendColumn("3dlu");
		builder.appendColumn("fill:max(pref; 100px)");
		
		builder.append("Type:", typeBox);
		builder.nextLine();
		
		builder.append("Range:", rangeBox);
		builder.nextLine();

		builder.append("Magnitude:", magnitudeField);
		builder.nextLine();

		builder.append("Duration:", durationField);
		builder.nextLine();
		
		builder.append("Target:", targetField);
		builder.nextLine();

		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(this);
		builder.append(saveButton);
		builder.nextLine();
		
		if (prev.isPresent()) {
			Effect effect = prev.get();
			typeBox.setSelectedItem(effect.getType());
			rangeBox.setSelectedItem(effect.getRange());
			magnitudeField.setText(effect.getMagnitude() + "");
			durationField.setText(effect.getDuration() + "");
			targetField.setText(effect.getTarget() + "");
		}

		add(builder.getPanel());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		/*
		Discipline discipline = (Discipline) disciplineBox.getSelectedItem();
		int value = Integer.parseInt(valueField.getText());
		int slots = Integer.parseInt(slotsField.getText());
		Requirement req = Requirement.newBuilder().setDiscipline(discipline)
				.setValue(value)
				.setSlots(slots)
				.build();
		owner.addAsset(prev, req);
		frame.dispose();
		*/
	}
}
