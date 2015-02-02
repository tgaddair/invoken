package com.eldritch.scifirpg.editor.tables;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.eldritch.scifirpg.editor.panel.DialogueEditorPanel;
import com.eldritch.invoken.proto.Actors.DialogueTree;
import com.google.common.base.Optional;

public class DialogueTable extends AssetTable<DialogueTree> {
	private static final long serialVersionUID = 1L;
	private static final String[] COLUMN_NAMES = { 
		"Responses", "Choices" };
	
	public DialogueTable() {
		super(COLUMN_NAMES, "Response");
	}
	
	public DialogueTree getAsset() {
		return getAssets().get(0);
	}

	@Override
	protected void handleCreateAsset(Optional<DialogueTree> asset) {
		// Create and set up the window.
        JFrame frame = new JFrame("Dialogue Editor");
        frame.add(new DialogueEditorPanel(this, asset), BorderLayout.CENTER);
        
        // Display the window.
        frame.pack();
        frame.setVisible(true);
	}

	@Override
	protected JPanel getEditorPanel(Optional<DialogueTree> prev, JFrame frame) {
		return new DialogueEditorPanel(this, prev);
	}
	
	@Override
	protected Object[] getDisplayFields(DialogueTree dialogue) {
		return new Object[]{dialogue.getDialogueCount(), dialogue.getChoiceCount()};
	}
}
