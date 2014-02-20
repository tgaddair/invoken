package com.eldritch.scifirpg.editor.asset;

import java.util.Arrays;
import java.util.Comparator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JTextField;

import com.eldritch.scifirpg.editor.tables.EffectTable;
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
	private final JTextField targetField = new JTextField();

	public EffectEditorPanel(EffectTable owner, JFrame frame, Optional<Effect> prev) {
		super(owner, frame, prev);

		DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(""));
		builder.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
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
		builder.append("Type:", typeBox);
		builder.nextLine();
		
		builder.append("Range:", rangeBox);
		builder.nextLine();

		builder.append("Magnitude:", magnitudeField);
		builder.nextLine();

		builder.append("Duration:", durationField);
		builder.nextLine();
		
		// TODO: this will need to become a dynamic combo box
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
			targetField.setText(effect.getTarget());
		}

		add(builder.getPanel());
	}

	@Override
	public Effect createAsset() {
		Type type = (Type) typeBox.getSelectedItem();
		Range range = (Range) rangeBox.getSelectedItem();
		int magnitude = Integer.parseInt(magnitudeField.getText());
		int duration = Integer.parseInt(durationField.getText());
		String target = targetField.getText();
		Effect effect = Effect.newBuilder()
				.setType(type)
				.setRange(range)
				.setMagnitude(magnitude)
				.setDuration(duration)
				.setTarget(target)
				.build();
		return effect;
	}
}
