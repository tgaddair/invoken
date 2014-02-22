package com.eldritch.scifirpg.editor.panel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JTextField;

import com.eldritch.scifirpg.editor.tables.OutcomeTable;
import com.eldritch.scifirpg.proto.Outcomes.Outcome;
import com.eldritch.scifirpg.proto.Outcomes.Outcome.Type;
import com.google.common.base.Optional;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class OutcomeEditorPanel extends AssetEditorPanel<Outcome, OutcomeTable> {
	private static final long serialVersionUID = 1L;

	private final JComboBox<Type> typeBox = new JComboBox<>(Type.values());
	private final JTextField targetField = new JTextField();
	private final JTextField countField = new JTextField();
	private final JTextField weightField = new JTextField();

	public OutcomeEditorPanel(OutcomeTable owner, JFrame frame, Optional<Outcome> prev) {
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

		builder.append("Count:", countField);
		builder.nextLine();
		
		builder.append("Weight:", weightField);
		builder.nextLine();

		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(this);
		builder.append(saveButton);
		builder.nextLine();
		
		if (prev.isPresent()) {
			Outcome asset = prev.get();
			typeBox.setSelectedItem(asset.getType());
			targetField.setText(asset.hasTarget() ? asset.getTarget() : "");
			countField.setText(asset.hasCount() ? asset.getCount() + "" : "");
			weightField.setText(asset.getWeight() + "");
		}

		add(builder.getPanel());
	}

	@Override
	public Outcome createAsset() {
		Type type = (Type) typeBox.getSelectedItem();
		Outcome.Builder builder = Outcome.newBuilder()
				.setType(type)
				.setWeight(Double.parseDouble(weightField.getText()));
		if (!targetField.getText().isEmpty()) {
			builder.setTarget(targetField.getText());
		}
		if (!countField.getText().isEmpty()) {
			builder.setCount(Integer.parseInt(countField.getText()));
		}
		return builder.build();
	}
}
