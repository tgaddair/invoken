package com.eldritch.scifirpg.editor.panel;

import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;

import com.eldritch.scifirpg.editor.tables.TraitTable;
import com.eldritch.invoken.proto.Actors.NonPlayerActor.Trait;
import com.eldritch.invoken.proto.Disciplines.Influence;
import com.google.common.base.Optional;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class TraitEditorPanel extends AssetEditorPanel<Trait, TraitTable> {
	private static final long serialVersionUID = 1L;

	private final JComboBox<Influence> influenceBox = new JComboBox<>(Influence.values());
	private final JCheckBox effectiveCheck = new JCheckBox();

	public TraitEditorPanel(TraitTable owner, JFrame frame, Optional<Trait> prev) {
		super(owner, frame, prev);

		DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(""));
		builder.border(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		builder.appendColumn("right:pref");
		builder.appendColumn("3dlu");
		builder.appendColumn("fill:max(pref; 100px)");
		
		List<Influence> values = new ArrayList<>();
		for (Influence i : Influence.values()) {
			if ((prev.isPresent() && prev.get().getInfluence() == i) || !owner.containsInfluence(i)) {
				values.add(i);
			}
		}
		influenceBox.setModel(new DefaultComboBoxModel<Influence>(values.toArray(new Influence[0])));
		builder.append("Influence:", influenceBox);
		builder.nextLine();

		builder.append("Effective:", effectiveCheck);
		builder.nextLine();

		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(this);
		builder.append(saveButton);
		builder.nextLine();
		
		if (prev.isPresent()) {
			Trait trait = prev.get();
			influenceBox.setSelectedItem(trait.getInfluence());
			effectiveCheck.setSelected(trait.getEffective());
		}

		add(builder.getPanel());
	}

	@Override
	public Trait createAsset() {
		Influence influence = (Influence) influenceBox.getSelectedItem();
		boolean effective = effectiveCheck.isSelected();
		return Trait.newBuilder().setInfluence(influence)
				.setEffective(effective)
				.build();
	}
}
