package com.eldritch.scifirpg.editor.panel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JTextField;

import com.eldritch.scifirpg.editor.MainPanel;
import com.eldritch.scifirpg.editor.tables.EffectTable;
import com.eldritch.scifirpg.proto.Disciplines.Influence;
import com.eldritch.scifirpg.proto.Effects.DamageType;
import com.eldritch.scifirpg.proto.Effects.Effect;
import com.eldritch.scifirpg.proto.Effects.Effect.Range;
import com.eldritch.scifirpg.proto.Effects.Effect.Type;
import com.google.common.base.Optional;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class EffectEditorPanel extends AssetEditorPanel<Effect, EffectTable> {
	private static final long serialVersionUID = 1L;

	private final JComboBox<Type> typeBox;
	private final JComboBox<Range> rangeBox = new JComboBox<>(Range.values());
	private final JTextField magnitudeField = new JTextField();
	private final JTextField durationField = new JTextField("0");
	private final JComboBox<String> targetBox = new JComboBox<String>();

	public EffectEditorPanel(EffectTable owner, JFrame frame, Optional<Effect> prev) {
		super(owner, frame, prev);

		DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(""));
		builder.border(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		builder.appendColumn("right:pref");
		builder.appendColumn("3dlu");
		builder.appendColumn("fill:max(pref; 100px)");
		
		Type[] types = Type.values();
		Arrays.sort(types, new Comparator<Type>() {
			@Override
			public int compare(Type t1, Type t2) {
				return t1.name().compareTo(t2.name());
			}
		});
		typeBox = new JComboBox<>(types);
		
		typeBox.addActionListener(new TypeSelectionListener());
		builder.append("Type:", typeBox);
		builder.nextLine();
		
		builder.append("Range:", rangeBox);
		builder.nextLine();

		builder.append("Magnitude:", magnitudeField);
		builder.nextLine();

		builder.append("Duration:", durationField);
		builder.nextLine();
		
		builder.append("Target:", targetBox);
		builder.nextLine();

		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(this);
		builder.append(saveButton);
		builder.nextLine();
		
		if (prev.isPresent()) {
			Effect effect = prev.get();
			initFieldsFor(effect.getType());
			typeBox.setSelectedItem(effect.getType());
			rangeBox.setSelectedItem(effect.getRange());
			magnitudeField.setText(effect.getMagnitude() + "");
			durationField.setText(effect.getDuration() + "");
			targetBox.setSelectedItem(effect.getTarget());
		} else {
			initFieldsFor((Type) typeBox.getSelectedItem());
		}

		add(builder.getPanel());
	}
	
	private void initFieldsFor(Type t) {
		List<String> values = new ArrayList<>();
		switch (t) {
			case DAMAGE_MELEE:
			case DAMAGE_RANGED:
			case DAMAGE_HEAVY:
			case DAMAGE_COORDINATED:
			case BARRIER:
			case MIRROR:
			case ABSORB:
				for (DamageType i : DamageType.values()) {
					values.add(i.name());
				}
				break;
			case INFLUENCE:
				for (Influence i : Influence.values()) {
					values.add(i.name());
				}
				break;
			case IMPERSONATE:
				values.addAll(MainPanel.FACTION_TABLE.getAssetIds());
				break;
			default:
		}
		targetBox.setEnabled(!values.isEmpty());
		targetBox.setModel(new DefaultComboBoxModel<String>(values.toArray(new String[0])));
	}

	@Override
	public Effect createAsset() {
		Type type = (Type) typeBox.getSelectedItem();
		Range range = (Range) rangeBox.getSelectedItem();
		int magnitude = Integer.parseInt(magnitudeField.getText());
		Effect.Builder builder = Effect.newBuilder()
				.setType(type)
				.setRange(range)
				.setMagnitude(magnitude);
		
		if (targetBox.isEnabled()) {
			String target = (String) targetBox.getSelectedItem();
			builder.setTarget(target);
		}
		
		// An empty duration means the effect is permanent
		if (!durationField.getText().isEmpty()) {
			int duration = Integer.parseInt(durationField.getText());
			builder.setDuration(duration);
		}
		
		return builder.build();
	}
	
	private class TypeSelectionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			Type t = (Type) typeBox.getSelectedItem();
			initFieldsFor(t);
		}
	}
}
