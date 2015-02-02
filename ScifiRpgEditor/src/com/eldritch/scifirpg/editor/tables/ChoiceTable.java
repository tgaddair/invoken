package com.eldritch.scifirpg.editor.tables;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.eldritch.invoken.proto.Actors.DialogueTree.Choice;
import com.eldritch.invoken.proto.Actors.DialogueTree.Response;
import com.eldritch.invoken.proto.Prerequisites.Prerequisite;
import com.eldritch.scifirpg.editor.AssetTablePanel;
import com.eldritch.scifirpg.editor.panel.AssetEditorPanel;
import com.eldritch.scifirpg.editor.panel.DialogueEditorPanel;
import com.google.common.base.Optional;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class ChoiceTable extends IdentifiedAssetTable<Choice> {
	private static final String[] COLUMN_NAMES = { 
		"Text", "Successors" };
	
	private static final long serialVersionUID = 1L;
	
	private final DialogueEditorPanel editor;
	
	public ChoiceTable(DialogueEditorPanel editor) {
		super(COLUMN_NAMES, "Choice");
		this.editor = editor;
	}
	
	public JPanel newEditorPanel(Optional<Choice> asset) {
		return new ChoiceEditorPanel(this, new JFrame(), asset);
	}
	
	@Override
	protected void handleCreateAsset(Optional<Choice> asset) {
		editor.editChoice(asset);
	}
	
	public List<Choice> getSortedAssets() {
		List<Choice> assets = new ArrayList<>(getAssets());
		Collections.sort(assets, new Comparator<Choice>() {
			@Override
			public int compare(Choice a1, Choice a2) {
				return Integer.compare(a1.getWeight(), a2.getWeight());
			}
		});
		return assets;
	}

	@Override
	protected JPanel getEditorPanel(Optional<Choice> prev, JFrame frame) {
		return new ChoiceEditorPanel(this, frame, prev);
	}
	
	@Override
	protected Object[] getDisplayFields(Choice asset) {
		String successors = "";
		for (String successor : asset.getSuccessorIdList()) {
			successors += successor + " ";
		}
		return new Object[]{asset.getText(), successors};
	}
	
	public class ChoiceEditorPanel extends AssetEditorPanel<Choice, ChoiceTable> {
		private static final long serialVersionUID = 1L;

		private final JTextField idField = new JTextField();
		private final JTextArea textField = createArea(true, 30, new Dimension(100, 100));
		private final JTextField weightField = new JTextField("0");
		private final PrerequisiteTable prereqTable = new PrerequisiteTable();
		private final AssetPointerTable<Response> successorTable;

		public ChoiceEditorPanel(ChoiceTable owner, JFrame frame, Optional<Choice> prev) {
			super(owner, frame, prev);

			DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(""));
			builder.border(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			builder.appendColumn("right:pref");
			builder.appendColumn("3dlu");
			builder.appendColumn("fill:max(pref; 100px)");
			
			builder.append("ID:", idField);
			builder.nextLine();

			builder.append("Text:", textField);
			builder.nextLine();

			builder.append("Weight:", weightField);
			builder.nextLine();
			
			builder.append("Prerequisites:", new AssetTablePanel(prereqTable));
			builder.nextLine();
			
			successorTable = new AssetPointerTable<Response>(editor.getResponseTable());
			builder.append("Successors:", new AssetTablePanel(successorTable));
			builder.nextLine();

			JButton saveButton = new JButton("Save");
			saveButton.addActionListener(this);
			builder.append(saveButton);
			builder.nextLine();
			
			if (prev.isPresent()) {
				Choice asset = prev.get();
				idField.setText(asset.getId());
				textField.setText(asset.getText());
				weightField.setText(asset.getWeight() + "");
				for (Prerequisite prereq : asset.getPrereqList()) {
					prereqTable.addAsset(prereq);
				}
				for (String s : asset.getSuccessorIdList()) {
					successorTable.addAssetId(s);
				}
			}

			add(builder.getPanel());
			setPreferredSize(new Dimension(650, 750));
		}

		@Override
		public Choice createAsset() {
			return Choice.newBuilder()
					.setId(idField.getText())
					.setText(textField.getText())
					.setWeight(Integer.parseInt(weightField.getText()))
					.addAllPrereq(prereqTable.getAssets())
					.addAllSuccessorId(successorTable.getAssetIds())
					.build();
		}
	}

	@Override
	protected String getAssetId(Choice asset) {
		return asset.getId();
	}
}
