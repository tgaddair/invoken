package com.eldritch.scifirpg.editor.panel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextField;

import com.eldritch.scifirpg.editor.tables.RankTable;
import com.eldritch.invoken.proto.Factions.Faction.Rank;
import com.google.common.base.Optional;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class RankEditorPanel extends AssetEditorPanel<Rank, RankTable> {
	private static final long serialVersionUID = 1L;

	private final JTextField idField = new JTextField();
	private final JTextField titleField = new JTextField();

	public RankEditorPanel(RankTable owner, JFrame frame, Optional<Rank> prev) {
		super(owner, frame, prev);

		DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(""));
		builder.border(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		builder.appendColumn("right:pref");
		builder.appendColumn("3dlu");
		builder.appendColumn("fill:max(pref; 100px)");

		builder.append("ID:", idField);
		builder.nextLine();

		builder.append("Title:", titleField);
		builder.nextLine();

		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(this);
		builder.append(saveButton);
		builder.nextLine();
		
		if (prev.isPresent()) {
			Rank asset = prev.get();
			idField.setText(asset.getId() + "");
			titleField.setText(asset.getTitle());
		}

		add(builder.getPanel());
	}

	@Override
	public Rank createAsset() {
		return Rank.newBuilder()
				.setId(Integer.parseInt(idField.getText()))
				.setTitle(titleField.getText())
				.build();
	}
}
