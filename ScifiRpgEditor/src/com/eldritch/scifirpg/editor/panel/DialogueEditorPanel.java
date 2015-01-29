package com.eldritch.scifirpg.editor.panel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;

import prefuse.controls.Control;
import prefuse.controls.ControlAdapter;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

import com.eldritch.invoken.proto.Actors.DialogueTree.Response;
import com.eldritch.scifirpg.editor.tables.DialogueTable;
import com.eldritch.scifirpg.editor.viz.DialogueEditor;
import com.eldritch.scifirpg.editor.viz.DialogueEditor.NodeType;
import com.google.common.base.Optional;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class DialogueEditorPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private final DialogueTable table;
	private final JPanel editorPanel = new JPanel(new BorderLayout());
	private DialogueEditor editor;
	private ResponseEditorPanel responseEditor;

	public DialogueEditorPanel(DialogueTable table) {
		super(new BorderLayout());
		this.table = table;
		responseEditor = new ResponseEditorPanel(table, this, new JFrame(), Optional.<Response>absent());
		
		if (!table.getAssets().isEmpty()) {
			editor = new DialogueEditor(table.getAssets(), new InfoClickListener());
			editorPanel.add(editor);
		}

		FormLayout layout = new FormLayout(
				"right:p, 4dlu, p, 7dlu, right:p, 4dlu, fill:default:grow, 4dlu, p", // columns
				"fill:default:grow"); // rows
		
		// Specify that columns 1 & 5 as well as 3 & 7 have equal widths.       
		layout.setColumnGroups(new int[][]{{1, 5}, {3, 7}});

		PanelBuilder builder = new PanelBuilder(layout);
		builder.border(Borders.DIALOG);
		CellConstraints cc = new CellConstraints();

		// column, row
		builder.add(responseEditor, cc.xy(3, 1));
		builder.add(editorPanel, cc.xy(7, 1));
		
//		JButton saveButton = new JButton("Save");
//		saveButton.addActionListener(this);
//		builder.add(saveButton, cc.xy(c + 4, 9));

		add(builder.getPanel());
		setPreferredSize(new Dimension(1400, 600));
	}
	
	public void handleSaveAction() {
		editorPanel.removeAll();
		editor = new DialogueEditor(table.getAssets(), new InfoClickListener());
		editorPanel.add(editor);
	}
	
	public class InfoClickListener extends ControlAdapter implements Control {
		public void itemClicked(VisualItem item, MouseEvent e) {
			if (item instanceof NodeItem) {
				String id = ((String) item.get("id"));
				
				NodeType type = ((NodeType) item.get("type"));
				if (type == NodeType.Choice) {
					id = (String) ((NodeItem) item).getParent().get("id");
				}

				Response response = editor.getResponse(id);
				responseEditor.init(response);
			}
		}
	}
}
