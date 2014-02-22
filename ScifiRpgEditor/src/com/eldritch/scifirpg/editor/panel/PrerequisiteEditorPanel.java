package com.eldritch.scifirpg.editor.panel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JTextField;

import com.eldritch.scifirpg.editor.tables.PrerequisiteTable;
import com.eldritch.scifirpg.proto.Prerequisites.Prerequisite;
import com.eldritch.scifirpg.proto.Prerequisites.Prerequisite.Type;
import com.google.common.base.Optional;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class PrerequisiteEditorPanel extends AssetEditorPanel<Prerequisite, PrerequisiteTable> {
	private static final long serialVersionUID = 1L;

	private final JComboBox<Type> typeBox = new JComboBox<>(Type.values());
	private final JTextField targetField = new JTextField();
	private final JTextField minField = new JTextField();
	private final JTextField maxField = new JTextField();
	private final JCheckBox notCheck = new JCheckBox();

	public PrerequisiteEditorPanel(PrerequisiteTable owner, JFrame frame, Optional<Prerequisite> prev) {
		super(owner, frame, prev);

		DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(""));
		builder.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		builder.appendColumn("right:pref");
		builder.appendColumn("3dlu");
		builder.appendColumn("fill:max(pref; 100px)");
		
		builder.append("Type:", typeBox);
		builder.nextLine();

		builder.append("Target:", targetField);
		builder.nextLine();

		builder.append("Min:", minField);
		builder.nextLine();
		
		builder.append("Max:", maxField);
		builder.nextLine();
		
		builder.append("Not:", notCheck);
		builder.nextLine();

		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(this);
		builder.append(saveButton);
		builder.nextLine();
		
		if (prev.isPresent()) {
			Prerequisite req = prev.get();
			typeBox.setSelectedItem(req.getType());
			targetField.setText(req.hasTarget() ? req.getTarget() : "");
			minField.setText(req.hasMin() ? req.getMin() + "" : "");
			minField.setText(req.hasMax() ? req.getMax() + "" : "");
			notCheck.setSelected(req.getNot());
		}

		add(builder.getPanel());
	}

	@Override
	public Prerequisite createAsset() {
		Type type = (Type) typeBox.getSelectedItem();
		Prerequisite.Builder builder = Prerequisite.newBuilder()
				.setType(type)
				.setNot(notCheck.isSelected());
		if (!targetField.getText().isEmpty()) {
			builder.setTarget(targetField.getText());
		}
		if (!minField.getText().isEmpty()) {
			builder.setMin(Integer.parseInt(minField.getText()));
		}
		if (!maxField.getText().isEmpty()) {
			builder.setMin(Integer.parseInt(maxField.getText()));
		}
		return builder.build();
	}
}
