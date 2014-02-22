package com.eldritch.scifirpg.editor.panel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JTextField;

import com.eldritch.scifirpg.editor.MainPanel;
import com.eldritch.scifirpg.editor.tables.FactionTable;
import com.eldritch.scifirpg.editor.tables.RelationTable;
import com.eldritch.scifirpg.proto.Factions.Faction.Relation;
import com.google.common.base.Optional;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class RelationEditorPanel extends AssetEditorPanel<Relation, RelationTable> {
	private static final long serialVersionUID = 1L;
	
	private final JComboBox<String> pointerBox = new JComboBox<String>();
	private final JTextField reactionField = new JTextField();
	
	public RelationEditorPanel(RelationTable table, JFrame frame, Optional<Relation> prev) {
		super(table, frame, prev);
		
		DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(""));
		builder.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		builder.appendColumn("right:pref");
		builder.appendColumn("3dlu");
		builder.appendColumn("fill:max(pref; 100px)");
		
		FactionTable majorTable = MainPanel.FACTION_TABLE;
		Set<String> currentIds = new HashSet<>();
		for (Relation r : table.getAssets()) {
			currentIds.add(r.getFactionId());
		}
		currentIds.add(table.getCurrentFactionId());
		List<String> values = new ArrayList<>();
		for (String id : majorTable.getAssetIds()) {
			if ((prev.isPresent() && prev.get().getFactionId().equals(id))
					|| !currentIds.contains(id)) {
				values.add(id);
			}
		}
		pointerBox.setModel(new DefaultComboBoxModel<String>(values.toArray(new String[0])));
		builder.append("Faction:", pointerBox);
		builder.nextLine();
		
		builder.append("Reaction:", reactionField);
		builder.nextLine();

		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(this);
		builder.append(saveButton);
		builder.nextLine();
		
		if (prev.isPresent()) {
			Relation asset = prev.get();
			pointerBox.setSelectedItem(asset.getFactionId());
			reactionField.setText(asset.getReaction() + "");
		}

		add(builder.getPanel());
	}

	@Override
	public Relation createAsset() {
		String id = (String) pointerBox.getSelectedItem();
		int reaction = Integer.parseInt(reactionField.getText());
		return Relation.newBuilder().setFactionId(id).setReaction(reaction).build();
	}
}
