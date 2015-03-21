package com.eldritch.scifirpg.editor.panel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import prefuse.controls.Control;
import prefuse.controls.ControlAdapter;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

import com.eldritch.invoken.proto.Actors.DialogueTree;
import com.eldritch.invoken.proto.Actors.DialogueTree.Choice;
import com.eldritch.invoken.proto.Actors.DialogueTree.Response;
import com.eldritch.scifirpg.editor.tables.ChoiceTable;
import com.eldritch.scifirpg.editor.tables.DialogueTable;
import com.eldritch.scifirpg.editor.tables.ResponseTable;
import com.eldritch.scifirpg.editor.util.DialogueConverter;
import com.eldritch.scifirpg.editor.viz.DialogueEditor;
import com.eldritch.scifirpg.editor.viz.DialogueEditor.NodeType;
import com.google.common.base.Optional;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class DialogueEditorPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private final Optional<DialogueTree> prev;
	private final JPanel leftPanel = new JPanel(new BorderLayout());
	private final JPanel editorPanel = new JPanel(new BorderLayout());
	private final DialogueTable table;
	private DialogueEditor editor;
	private final ResponseTable responses;
	private final ChoiceTable choices;

	public DialogueEditorPanel(DialogueTable table, Optional<DialogueTree> prev) {
		super(new BorderLayout());
		this.prev = prev;
		this.table = table;
		
		responses = new ResponseTable(this);
		choices = new ChoiceTable(this);
		leftPanel.add(responses.newEditorPanel(Optional.<Response>absent()));
		
		if (prev.isPresent()) {
			DialogueTree tree = DialogueConverter.convert(prev.get());
			if (tree.getDialogueCount() > 0) {
				for (Response response : tree.getDialogueList()) {
					responses.addAsset(response);
				}
				for (Choice choice : tree.getChoiceList()) {
					choices.addAsset(choice);
				}
				editor = new DialogueEditor(tree, new InfoClickListener());
				editorPanel.add(editor);
			}
		}

		FormLayout layout = new FormLayout(
				"right:p, 4dlu, p, 7dlu, right:p, 4dlu, fill:default:grow, 4dlu, p", // columns
				"fill:default:grow, 3dlu, p, 3dlu, p"); // rows
		
		// Specify that columns 1 & 5 as well as 3 & 7 have equal widths.       
		layout.setColumnGroups(new int[][]{{1, 5}, {3, 7}});

		PanelBuilder builder = new PanelBuilder(layout);
		builder.border(Borders.DIALOG);
		CellConstraints cc = new CellConstraints();

		// column, row
		builder.add(leftPanel, cc.xy(3, 1));
		builder.add(editorPanel, cc.xy(7, 1));
		
		JButton responseButton = new JButton("New Response");
		responseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				editResponse(Optional.<Response>absent());
			}
	    });
		builder.add(responseButton, cc.xy(3, 3));
		
		JButton choiceButton = new JButton("New Choice");
		choiceButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				editChoice(Optional.<Choice>absent());
			}
	    });
		builder.add(choiceButton, cc.xy(7, 3));

		add(builder.getPanel());
		setPreferredSize(new Dimension(1400, 700));
	}
	
	public ResponseTable getResponseTable() {
		return responses;
	}
	
	public ChoiceTable getChoiceTable() {
		return choices;
	}
	
	public void handleSaveAction() {
		DialogueTree tree = createDialogue();
		table.addAsset(prev, tree);
		
		editorPanel.removeAll();
		editor = new DialogueEditor(tree, new InfoClickListener());
		editorPanel.add(editor);
		editorPanel.invalidate();
		editorPanel.validate();
		editorPanel.repaint();
	}
	
	private DialogueTree createDialogue() {
		DialogueTree.Builder builder = DialogueTree.newBuilder();
		builder.addAllDialogue(responses.getSortedAssets());
		builder.addAllChoice(choices.getSortedAssets());
		return builder.build();
	}
	
	public void editResponse(Optional<Response> response) {
		leftPanel.removeAll();
		leftPanel.add(responses.newEditorPanel(response));
		leftPanel.invalidate();
		leftPanel.validate();
		leftPanel.repaint();
	}
	
	public void editChoice(Optional<Choice> choice) {
		leftPanel.removeAll();
		leftPanel.add(choices.newEditorPanel(choice));
		leftPanel.invalidate();
		leftPanel.validate();
		leftPanel.repaint();
	}
	
	public class InfoClickListener extends ControlAdapter implements Control {
		public void itemClicked(VisualItem item, MouseEvent e) {
			if (item instanceof NodeItem) {
				String id = ((String) item.get("id"));
				
				NodeType type = ((NodeType) item.get("type"));
				if (type == NodeType.Choice) {
					Choice choice = editor.getChoice(id);
					editChoice(Optional.of(choice));
				} else {
					Response response = editor.getResponse(id);
					editResponse(Optional.of(response));
				}

				if (e.isControlDown()) {
					JPopupMenu popup = new JPopupMenu();
					
					JMenuItem responseItem = new JMenuItem("New Response");
					responseItem.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent ev) {
							editResponse(Optional.<Response>absent());
						}
				    });
					popup.add(responseItem);
					
					JMenuItem choiceItem = new JMenuItem("New Choice");
					choiceItem.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent ev) {
							editChoice(Optional.<Choice>absent());
						}
				    });
					popup.add(choiceItem);
					
					popup.show(e.getComponent(), (int) e.getX(), (int) e.getY());
				}
			}
		}
	}
}
