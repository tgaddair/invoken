package com.eldritch.scifirpg.editor.panel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JTextField;

import com.eldritch.scifirpg.editor.MainPanel;
import com.eldritch.scifirpg.editor.tables.OutcomeTable;
import com.eldritch.scifirpg.proto.Disciplines.Discipline;
import com.eldritch.scifirpg.proto.Outcomes.Outcome;
import com.eldritch.scifirpg.proto.Outcomes.Outcome.Type;
import com.google.common.base.Optional;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class OutcomeEditorPanel extends AssetEditorPanel<Outcome, OutcomeTable> {
	private static final long serialVersionUID = 1L;

	private final JComboBox<Type> typeBox = new JComboBox<>(Type.values());
	private final JComboBox<String> targetBox = new JComboBox<String>();
	private final JTextField countField = new JTextField();
	private final JTextField weightField = new JTextField();

	public OutcomeEditorPanel(OutcomeTable owner, JFrame frame, Optional<Outcome> prev) {
		super(owner, frame, prev);

		DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(""));
		builder.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		builder.appendColumn("right:pref");
		builder.appendColumn("3dlu");
		builder.appendColumn("fill:max(pref; 100px)");
		
		typeBox.addActionListener(new TypeSelectionListener());
		builder.append("Type:", typeBox);
		builder.nextLine();

		builder.append("Target:", targetBox);
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
			initFieldsFor((Type) typeBox.getSelectedItem());
			if (asset.hasTarget()) {
				targetBox.setSelectedItem(asset.getTarget());
			}
			countField.setText(asset.hasCount() ? asset.getCount() + "" : "");
			weightField.setText(asset.getWeight() + "");
		} else {
			initFieldsFor((Type) typeBox.getSelectedItem());
		}
		
		add(builder.getPanel());
		setPreferredSize(new Dimension(500, 500));
	}

	@Override
	public Outcome createAsset() {
		Type type = (Type) typeBox.getSelectedItem();
		Outcome.Builder builder = Outcome.newBuilder()
				.setType(type)
				.setWeight(Double.parseDouble(weightField.getText()));
		if (targetBox.isEnabled()) {
			builder.setTarget((String) targetBox.getSelectedItem());
		}
		if (!countField.isEnabled() && !countField.getText().isEmpty()) {
			builder.setCount(Integer.parseInt(countField.getText()));
		}
		return builder.build();
	}
	
	private void initFieldsFor(Type t) {
		List<String> values = new ArrayList<>();
		boolean targetEnabled = true;
		boolean countEnabled = true;
		
		switch (t) {
			case ITEM_CHANGE:
				break;
			case REP_CHANGE:
				values.addAll(MainPanel.FACTION_TABLE.getAssetIds());
				break;
			case XP_GAIN:
				for (Discipline d : Discipline.values()) {
					values.add(d.name());
				}
				break;
			case AUG_GAIN:
				countEnabled = false;
			case AUG_USE:
				values.addAll(MainPanel.AUGMENTATION_TABLE.getAssetIds());
				break;
			case TELEPORT:
				countEnabled = false;
				break;
			// No target
			case INFLUENCE_RESET:
				countEnabled = false;
			case HP_CHANGE:
			case INFLUENCE_MOD:
				targetEnabled = false;
				break;
			default:
				throw new IllegalStateException("Unrecognized Prerequisite Type: " + t);
		}
		
		targetBox.setEnabled(targetEnabled);
		countField.setEnabled(countEnabled);
		targetBox.setModel(new DefaultComboBoxModel<String>(values.toArray(new String[0])));
	}
	
	private class TypeSelectionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			Type t = (Type) typeBox.getSelectedItem();
			initFieldsFor(t);
		}
	}
}
