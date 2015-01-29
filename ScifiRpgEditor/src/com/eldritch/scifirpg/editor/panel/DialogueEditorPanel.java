package com.eldritch.scifirpg.editor.panel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.eldritch.scifirpg.editor.AssetTablePanel;
import com.eldritch.scifirpg.editor.tables.AugmentationTable;
import com.eldritch.scifirpg.editor.tables.DialogueTable;
import com.eldritch.scifirpg.editor.tables.EffectTable;
import com.eldritch.scifirpg.editor.tables.RequirementTable;
import com.eldritch.scifirpg.editor.viz.DialogueEditor;
import com.eldritch.invoken.proto.Augmentations.Augmentation;
import com.eldritch.invoken.proto.Augmentations.Augmentation.Requirement;
import com.eldritch.invoken.proto.Augmentations.Augmentation.Type;
import com.eldritch.invoken.proto.Effects.Effect;
import com.google.common.base.Optional;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class DialogueEditorPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private final JTextField idField = new JTextField();
	private final DialogueEditor editor;
	private final ResponseEditorPanel responseEditor;

	public DialogueEditorPanel(DialogueTable table) {
		super(new BorderLayout());
		editor = new DialogueEditor(table.getAssets());
		responseEditor = table.getEditorPanel();

		FormLayout layout = new FormLayout(
				"right:p, 4dlu, p, 7dlu, right:p, 4dlu, fill:default:grow, 4dlu, p", // columns
				"fill:default:grow"); // rows
		
		// Specify that columns 1 & 5 as well as 3 & 7 have equal widths.       
		layout.setColumnGroups(new int[][]{{1, 5}, {3, 7}});

		PanelBuilder builder = new PanelBuilder(layout);
		builder.border(Borders.DIALOG);
		CellConstraints cc = new CellConstraints();
		int r = 1;
		int c = 1;
		
		builder.add(responseEditor, cc.xy(c + 2, r));
		
		c = 5;
		r = 1;
		
		builder.add(editor, cc.xy(c + 2, r));
		
//		JButton saveButton = new JButton("Save");
//		saveButton.addActionListener(this);
//		builder.add(saveButton, cc.xy(c + 4, 9));

		add(builder.getPanel());
		setPreferredSize(new Dimension(1400, 600));
	}
}
