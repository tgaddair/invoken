package com.eldritch.scifirpg.editor.tables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.eldritch.scifirpg.editor.panel.DialogueEditorPanel;
import com.eldritch.scifirpg.editor.panel.ResponseEditorPanel;
import com.eldritch.invoken.proto.Actors.DialogueTree.Response;
import com.google.common.base.Optional;

public class ResponseTable extends IdentifiedAssetTable<Response> {
	private static final long serialVersionUID = 1L;
	private static final String[] COLUMN_NAMES = { 
		"Text", "Successors" };
	
	private final DialogueEditorPanel editor;
	
	public ResponseTable(DialogueEditorPanel editor) {
		super(COLUMN_NAMES, "Response");
		this.editor = editor;
	}
	
	public List<Response> getSortedAssets() {
		List<Response> assets = new ArrayList<>(getAssets());
		Collections.sort(assets, new Comparator<Response>() {
			@Override
			public int compare(Response a1, Response a2) {
				return Integer.compare(a1.getWeight(), a2.getWeight());
			}
		});
		return assets;
	}
	
	public JPanel newEditorPanel(Optional<Response> asset) {
		return new ResponseEditorPanel(this, editor, new JFrame(), asset);
	}

	@Override
	protected void handleCreateAsset(Optional<Response> asset) {
		// Create and set up the window.
        JFrame frame = new JFrame("Response Editor");
        frame.add(new ResponseEditorPanel(this, editor, frame, asset));
        
        // Display the window.
        frame.pack();
        frame.setVisible(true);
	}
	

	@Override
	protected JPanel getEditorPanel(Optional<Response> asset, JFrame frame) {
		return new ResponseEditorPanel(this, editor, frame, asset);
	}
	
	@Override
	protected Object[] getDisplayFields(Response response) {
		String successors = "";
		for (String successor : response.getChoiceIdList()) {
			successors += successor + " ";
		}
		return new Object[]{response.getText(), successors};
	}

	@Override
	protected String getAssetId(Response asset) {
		return asset.getId();
	}
}
