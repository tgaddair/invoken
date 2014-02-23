package com.eldritch.scifirpg.editor.panel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JTextField;

import com.eldritch.scifirpg.editor.MainPanel;
import com.eldritch.scifirpg.editor.tables.PrerequisiteTable;
import com.eldritch.scifirpg.proto.Disciplines.Discipline;
import com.eldritch.scifirpg.proto.Disciplines.Influence;
import com.eldritch.scifirpg.proto.Prerequisites.Prerequisite;
import com.eldritch.scifirpg.proto.Prerequisites.Prerequisite.Type;
import com.eldritch.scifirpg.proto.Prerequisites.Standing;
import com.google.common.base.Optional;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class PrerequisiteEditorPanel extends AssetEditorPanel<Prerequisite, PrerequisiteTable> {
	private static final long serialVersionUID = 1L;

	private final JComboBox<Type> typeBox = new JComboBox<>(Type.values());
	private final JComboBox<String> targetBox = new JComboBox<String>();
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
		
		typeBox.addActionListener(new TypeSelectionListener());
		builder.append("Type:", typeBox);
		builder.nextLine();

		builder.append("Target:", targetBox);
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
			initFieldsFor((Type) typeBox.getSelectedItem());
			if (req.hasTarget()) {
				targetBox.setSelectedItem(req.getTarget());
			}
			minField.setText(req.hasMin() ? req.getMin() + "" : "");
			maxField.setText(req.hasMax() ? req.getMax() + "" : "");
			notCheck.setSelected(req.getNot());
		} else {
			initFieldsFor((Type) typeBox.getSelectedItem());
		}

		add(builder.getPanel());
		setPreferredSize(new Dimension(500, 500));
	}

	@Override
	public Prerequisite createAsset() {
		Type type = (Type) typeBox.getSelectedItem();
		Prerequisite.Builder builder = Prerequisite.newBuilder()
				.setType(type)
				.setNot(notCheck.isSelected());
		if (targetBox.isEnabled()) {
			builder.setTarget((String) targetBox.getSelectedItem());
		}
		if (minField.isEnabled() && !minField.getText().isEmpty()) {
			builder.setMin(Integer.parseInt(minField.getText()));
		}
		if (maxField.isEnabled() && !maxField.getText().isEmpty()) {
			builder.setMax(Integer.parseInt(maxField.getText()));
		}
		return builder.build();
	}
	
	private void initFieldsFor(Type t) {
		List<String> values = new ArrayList<>();
		boolean targetEnabled = true;
		boolean minEnabled = true;
		boolean maxEnabled = true;
		
		switch (t) {
			case DISCIPLINE_BETWEEN:
				for (Discipline d : Discipline.values()) {
					values.add(d.name());
				}
				break;
			case REP_BETWEEN:
			case RANK_BETWEEN:
				values.addAll(MainPanel.FACTION_TABLE.getAssetIds());
				break;
			case MISSION_STAGE:
				values.addAll(MainPanel.MISSION_TABLE.getAssetIds());
				break;
			case LVL_BETWEEN:
			case INFLUENCE_BETWEEN:
				targetEnabled = false;
				break;
			case AUG_REMAINING:
				values.addAll(MainPanel.AUGMENTATION_TABLE.getAssetIds());
				maxEnabled = false;
				break;
			case INFLUENCE_AUG:
				for (Influence i : Influence.values()) {
					values.add(i.name());
				}
				minEnabled = false;
				maxEnabled = false;
				break;
			case ENCOUNTER_SEEN:
				//values.addAll(MainPanel.ENCOUNTER_TABLE.getAssetIds());
			case DIALOGUE_SEEN:
				//values.addAll(getCurrentActor().getDialogueTable());
				targetEnabled = false;
				break;
			case ITEM_EQUIPPED:
				minEnabled = false;
			case ITEM_HAS:
				maxEnabled = false;
				//values.addAll(MainPanel.ITEM_TABLE.getAssetIds());
				break;
			case STANDING_IS:
				for (Standing s : Standing.values()) {
					values.add(s.name());
				}
				minEnabled = false;
				maxEnabled = false;
				break;
			default:
				throw new IllegalStateException("Unrecognized Prerequisite Type: " + t);
		}
		
		targetBox.setEnabled(targetEnabled);
		minField.setEnabled(minEnabled);
		maxField.setEnabled(maxEnabled);
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
