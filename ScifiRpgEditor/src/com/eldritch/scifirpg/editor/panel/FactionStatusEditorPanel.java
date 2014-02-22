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
import com.eldritch.scifirpg.editor.tables.FactionStatusTable;
import com.eldritch.scifirpg.editor.tables.FactionTable;
import com.eldritch.scifirpg.proto.Actors.ActorParams.FactionStatus;
import com.google.common.base.Optional;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class FactionStatusEditorPanel extends AssetEditorPanel<FactionStatus, FactionStatusTable> {
	private static final long serialVersionUID = 1L;
	
	private final JComboBox<String> pointerBox = new JComboBox<String>();
	private final JTextField reputationField = new JTextField();
	private final JTextField rankField = new JTextField();
	
	public FactionStatusEditorPanel(FactionStatusTable table, JFrame frame, Optional<FactionStatus> prev) {
		super(table, frame, prev);
		
		DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(""));
		builder.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		builder.appendColumn("right:pref");
		builder.appendColumn("3dlu");
		builder.appendColumn("fill:max(pref; 100px)");
		
		FactionTable majorTable = MainPanel.FACTION_TABLE;
		Set<String> currentIds = new HashSet<>();
		for (FactionStatus status : table.getAssets()) {
			currentIds.add(status.getFactionId());
		}
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
		
		builder.append("Reputation:", reputationField);
		builder.nextLine();
		
		builder.append("Rank:", rankField);
		builder.nextLine();

		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(this);
		builder.append(saveButton);
		builder.nextLine();
		
		if (prev.isPresent()) {
			FactionStatus asset = prev.get();
			pointerBox.setSelectedItem(asset.getFactionId());
			if (asset.hasReputation()) {
				reputationField.setText(asset.getReputation() + "");
			}
			if (asset.hasRank()) {
				rankField.setText(asset.getRank() + "");
			}
		}

		add(builder.getPanel());
	}

	@Override
	public FactionStatus createAsset() {
		String id = (String) pointerBox.getSelectedItem();
		FactionStatus.Builder builder = FactionStatus.newBuilder().setFactionId(id);
		if (!reputationField.getText().isEmpty()) {
			builder.setReputation(Integer.parseInt(reputationField.getText()));
		}
		if (!rankField.getText().isEmpty()) {
			builder.setRank(Integer.parseInt(rankField.getText()));
		}
		return builder.build();
	}
}
