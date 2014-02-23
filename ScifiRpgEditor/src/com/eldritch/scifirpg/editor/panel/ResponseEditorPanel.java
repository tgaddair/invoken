package com.eldritch.scifirpg.editor.panel;

import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.eldritch.scifirpg.editor.AssetTablePanel;
import com.eldritch.scifirpg.editor.tables.AssetPointerTable;
import com.eldritch.scifirpg.editor.tables.AssetTable;
import com.eldritch.scifirpg.editor.tables.DialogueTable;
import com.eldritch.scifirpg.editor.tables.OutcomeTable;
import com.eldritch.scifirpg.editor.tables.PrerequisiteTable;
import com.eldritch.scifirpg.proto.Actors.DialogueTree.Choice;
import com.eldritch.scifirpg.proto.Actors.DialogueTree.Response;
import com.eldritch.scifirpg.proto.Outcomes.Outcome;
import com.eldritch.scifirpg.proto.Prerequisites.Prerequisite;
import com.google.common.base.Optional;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class ResponseEditorPanel extends AssetEditorPanel<Response, DialogueTable> {
	private static final long serialVersionUID = 1L;

	private final JTextField idField = new JTextField();
	private final JTextArea textField = createArea(true, 30, new Dimension(100, 100));
	private final JCheckBox greetingCheck = new JCheckBox();
	private final PrerequisiteTable prereqTable = new PrerequisiteTable();
	private final OutcomeTable outcomeTable = new OutcomeTable();
	private final ChoiceTable choiceTable;

	public ResponseEditorPanel(DialogueTable owner, JFrame frame, Optional<Response> prev) {
		super(owner, frame, prev);

		DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(""));
		builder.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		builder.appendColumn("right:pref");
		builder.appendColumn("3dlu");
		builder.appendColumn("fill:max(pref; 100px)");
		
		builder.append("ID:", idField);
		builder.nextLine();

		builder.append("Text:", textField);
		builder.nextLine();

		builder.append("Greeting:", greetingCheck);
		builder.nextLine();
		
		builder.append("Prerequisites:", new AssetTablePanel(prereqTable));
		builder.nextLine();
		
		builder.append("Outcomes:", new AssetTablePanel(outcomeTable));
		builder.nextLine();
		
		builder.appendRow("fill:120dlu");
		choiceTable = new ChoiceTable(owner);
		builder.append("Choices:", new AssetTablePanel(choiceTable));
		builder.nextLine();

		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(this);
		builder.append(saveButton);
		builder.nextLine();
		
		if (prev.isPresent()) {
			Response resp = prev.get();
			idField.setText(resp.getId());
			textField.setText(resp.getText());
			greetingCheck.setSelected(resp.getGreeting());
			for (Prerequisite asset : resp.getPrereqList()) {
				prereqTable.addAsset(asset);
			}
			for (Outcome asset : resp.getOutcomeList()) {
				outcomeTable.addAsset(asset);
			}
			for (Choice asset : resp.getChoiceList()) {
				choiceTable.addAsset(asset);
			}
		}

		add(builder.getPanel());
		setPreferredSize(new Dimension(650, 750));
	}

	@Override
	public Response createAsset() {
		String id = idField.getText();
		String text = textField.getText();
		boolean greeting = greetingCheck.isSelected();
		return Response.newBuilder()
				.setId(id)
				.setText(text)
				.setGreeting(greeting)
				.addAllPrereq(prereqTable.getAssets())
				.addAllOutcome(outcomeTable.getAssets())
				.addAllChoice(choiceTable.getAssets())
				.build();
	}
	
	private static class ChoiceTable extends AssetTable<Choice> {
		private static final long serialVersionUID = 1L;
		private static final String[] COLUMN_NAMES = { 
			"Text", "Successors" };
		
		private final DialogueTable dialogueTable;
		
		public ChoiceTable(DialogueTable dialogueTable) {
			super(COLUMN_NAMES, "Choice");
			this.dialogueTable = dialogueTable;
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
	}
	
	private static class ChoiceEditorPanel extends AssetEditorPanel<Choice, ChoiceTable> {
		private static final long serialVersionUID = 1L;

		private final JTextArea textField = createArea(true, 30, new Dimension(100, 100));
		private final PrerequisiteTable prereqTable = new PrerequisiteTable();
		private final AssetPointerTable<Response> successorTable;

		public ChoiceEditorPanel(ChoiceTable owner, JFrame frame, Optional<Choice> prev) {
			super(owner, frame, prev);

			DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(""));
			builder.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			builder.appendColumn("right:pref");
			builder.appendColumn("3dlu");
			builder.appendColumn("fill:max(pref; 100px)");

			builder.append("Text:", textField);
			builder.nextLine();
			
			builder.append("Prerequisites:", new AssetTablePanel(prereqTable));
			builder.nextLine();
			
			successorTable = new AssetPointerTable<Response>(owner.dialogueTable);
			builder.append("Successors:", new AssetTablePanel(successorTable));
			builder.nextLine();

			JButton saveButton = new JButton("Save");
			saveButton.addActionListener(this);
			builder.append(saveButton);
			builder.nextLine();
			
			if (prev.isPresent()) {
				Choice asset = prev.get();
				textField.setText(asset.getText());
				for (Prerequisite prereq : asset.getPrereqList()) {
					prereqTable.addAsset(prereq);
				}
				for (String s : asset.getSuccessorIdList()) {
					successorTable.addAssetId(s);
				}
			}

			add(builder.getPanel());
			setPreferredSize(new Dimension(700, 500));
		}

		@Override
		public Choice createAsset() {
			return Choice.newBuilder()
					.setText(textField.getText())
					.addAllPrereq(prereqTable.getAssets())
					.addAllSuccessorId(successorTable.getAssetIds())
					.build();
		}
	}
}
