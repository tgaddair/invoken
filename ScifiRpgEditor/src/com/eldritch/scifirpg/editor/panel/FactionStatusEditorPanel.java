package com.eldritch.scifirpg.editor.panel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import com.eldritch.scifirpg.proto.Factions.Faction;
import com.eldritch.scifirpg.proto.Factions.Faction.Rank;
import com.google.common.base.Optional;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class FactionStatusEditorPanel extends AssetEditorPanel<FactionStatus, FactionStatusTable> {
	private static final long serialVersionUID = 1L;
	
	private final JComboBox<String> pointerBox = new JComboBox<String>();
	private final JTextField reputationField = new JTextField();
	private final JComboBox<RankWrapper> rankBox = new JComboBox<RankWrapper>();
	
	public FactionStatusEditorPanel(FactionStatusTable table, JFrame frame, Optional<FactionStatus> prev) {
		super(table, frame, prev);
		
		DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(""));
		builder.border(BorderFactory.createEmptyBorder(5, 5, 5, 5));
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
		pointerBox.addActionListener(new RankFillListener());
		builder.append("Faction:", pointerBox);
		builder.nextLine();
		
		builder.append("Reputation:", reputationField);
		builder.nextLine();
		
		builder.append("Rank:", rankBox);
		builder.nextLine();

		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(this);
		builder.append(saveButton);
		builder.nextLine();
		
		if (prev.isPresent()) {
			FactionStatus asset = prev.get();
			pointerBox.setSelectedItem(asset.getFactionId());
			fillRanks(asset.getFactionId());
			if (asset.hasReputation()) {
				reputationField.setText(asset.getReputation() + "");
			}
			if (asset.hasRank()) {
				RankWrapper rank = new RankWrapper(Rank.newBuilder()
						.setId(asset.getRank())
						.setTitle("?")
						.build());
				rankBox.setSelectedItem(rank);
			}
		} else {
			fillRanks((String) pointerBox.getSelectedItem());
		}

		add(builder.getPanel());
	}
	
	private void fillRanks(String factionId) {
		FactionTable majorTable = MainPanel.FACTION_TABLE;
		for (Faction asset : majorTable.getAssets()) {
			if (asset.getId().equals(factionId)) {
				List<RankWrapper> values = new ArrayList<>();
				values.add(new RankWrapper(null)); // No rank
				for (Rank r : asset.getRankList()) {
					values.add(new RankWrapper(r));
				}
				rankBox.setModel(new DefaultComboBoxModel<RankWrapper>(values.toArray(new RankWrapper[0])));
			}
		}
	}
	
	private class RankFillListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			String factionId = (String) pointerBox.getSelectedItem();
			fillRanks(factionId);
		}
	}

	@Override
	public FactionStatus createAsset() {
		String id = (String) pointerBox.getSelectedItem();
		FactionStatus.Builder builder = FactionStatus.newBuilder().setFactionId(id);
		if (!reputationField.getText().isEmpty()) {
			builder.setReputation(Integer.parseInt(reputationField.getText()));
		}
		RankWrapper rank = (RankWrapper) rankBox.getSelectedItem();
		if (rank != null && rank.hasRank()) {
			builder.setRank(rank.getRankId());
		}
		return builder.build();
	}
	
	private static class RankWrapper {
		public final Rank rank;
		
		public RankWrapper(Rank rank) {
			this.rank = rank;
		}
		
		public boolean hasRank() {
			return rank != null;
		}
		
		public int getRankId() {
			return rank.getId();
		}
		
		@Override
		public String toString() {
			if (rank == null) {
				return "";
			}
			return String.format("%d - %s", rank.getId(), rank.getTitle());
		}

		@Override
		public int hashCode() {
			Integer i = rank == null ? 0 : rank.getId();
			return i.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			RankWrapper other = (RankWrapper) obj;
			if (rank == null) {
				if (other.rank != null)
					return false;
			} else if (other.rank == null) {
				return false;
			} else if (rank.getId() != other.rank.getId()) {
				return false;
			}
			return true;
		}
	}
}
