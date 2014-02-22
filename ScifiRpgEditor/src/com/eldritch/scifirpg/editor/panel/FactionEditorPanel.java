package com.eldritch.scifirpg.editor.panel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JTextField;

import com.eldritch.scifirpg.editor.AssetTablePanel;
import com.eldritch.scifirpg.editor.tables.FactionTable;
import com.eldritch.scifirpg.editor.tables.RankTable;
import com.eldritch.scifirpg.editor.tables.RelationTable;
import com.eldritch.scifirpg.proto.Disciplines.Profession;
import com.eldritch.scifirpg.proto.Factions.Faction;
import com.eldritch.scifirpg.proto.Factions.Faction.Rank;
import com.eldritch.scifirpg.proto.Factions.Faction.Relation;
import com.google.common.base.Optional;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class FactionEditorPanel extends AssetEditorPanel<Faction, FactionTable> {
	private static final long serialVersionUID = 1L;

	private final JTextField idField = new JTextField();
	private final JTextField nameField = new JTextField();
	private final JCheckBox unalignedCheck = new JCheckBox();
	private final JComboBox<Profession> alignmentBox = new JComboBox<Profession>(Profession.values());
	private final RankTable rankTable = new RankTable();
	private final RelationTable relationTable = new RelationTable(this);

	public FactionEditorPanel(FactionTable owner, JFrame frame, Optional<Faction> prev) {
		super(owner, frame, prev);

		DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(""));
		builder.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		builder.appendColumn("right:pref");
		builder.appendColumn("3dlu");
		builder.appendColumn("fill:max(pref; 100px)");
		
		nameField.addActionListener(new NameTypedListener(idField));
		builder.append("Name:", nameField);
		builder.nextLine();
		
		builder.append("ID:", idField);
		builder.nextLine();

		unalignedCheck.addActionListener(new CheckListener());
		builder.append("Unaligned:", unalignedCheck);
		builder.nextLine();
		
		builder.append("Alignment:", alignmentBox);
		builder.nextLine();
		
		builder.append("Ranks:", new AssetTablePanel(rankTable));
		builder.nextLine();
		
		builder.append("Relations:", new AssetTablePanel(relationTable));
		builder.nextLine();

		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(this);
		builder.append(saveButton);
		builder.nextLine();
		
		if (prev.isPresent()) {
			Faction asset = prev.get();
			idField.setText(asset.getId());
			nameField.setText(asset.getName());
			if (asset.hasAlignment()) {
				unalignedCheck.setSelected(false);
				alignmentBox.setSelectedItem(asset.getAlignment());
			} else {
				unalignedCheck.setSelected(true);
			}
			for (Rank rank : asset.getRankList()) {
				rankTable.addAsset(rank);
			}
			for (Relation relation : asset.getRelationList()) {
				relationTable.addAsset(relation);
			}
		}

		add(builder.getPanel());
		setPreferredSize(new Dimension(650, 750));
	}
	
	
	public String getCurrentFactionId() {
		return idField.getText();
	}

	@Override
	public Faction createAsset() {
		String id = idField.getText();
		String name = nameField.getText();
		Faction.Builder builder = Faction.newBuilder()
				.setId(id)
				.setName(name)
				.addAllRank(rankTable.getSortedAssets())
				.addAllRelation(relationTable.getAssets());
		if (!unalignedCheck.isSelected()) {
			builder.setAlignment((Profession) alignmentBox.getSelectedItem());
		}
		return builder.build();
	}
	
	private class CheckListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			boolean selected = unalignedCheck.isSelected();
			alignmentBox.setEnabled(!selected);
		}
	}
}
