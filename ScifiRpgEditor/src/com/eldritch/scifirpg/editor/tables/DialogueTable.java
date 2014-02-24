package com.eldritch.scifirpg.editor.tables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.eldritch.scifirpg.editor.panel.ResponseEditorPanel;
import com.eldritch.scifirpg.proto.Actors.DialogueTree.Response;
import com.google.common.base.Optional;

public class DialogueTable extends IdentifiedAssetTable<Response> {
	private static final long serialVersionUID = 1L;
	private static final String[] COLUMN_NAMES = { 
		"ID", "Text", "Greeting" };
	
	public DialogueTable() {
		super(COLUMN_NAMES, "Response");
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


	@Override
	protected JPanel getEditorPanel(Optional<Response> prev, JFrame frame) {
		return new ResponseEditorPanel(this, frame, prev);
	}
	
	@Override
	protected Object[] getDisplayFields(Response resp) {
		Object greeting = resp.getGreeting() ? "yes" : "";
		return new Object[]{resp.getId(), resp.getText(), greeting};
	}

	@Override
	protected String getAssetId(Response asset) {
		return asset.getId();
	}
}
