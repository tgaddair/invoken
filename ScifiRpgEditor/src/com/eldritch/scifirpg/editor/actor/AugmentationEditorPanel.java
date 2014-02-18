package com.eldritch.scifirpg.editor.actor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.eldritch.scifirpg.proto.Augmentations.Augmentation.Type;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class AugmentationEditorPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	public AugmentationEditorPanel() {
		super(new BorderLayout());

		FormLayout layout = new FormLayout(
				"right:p, 4dlu, p, 7dlu, right:p, 4dlu, p, 4dlu, p", // columns
				"p, 3dlu, p, 3dlu, fill:default:grow, 3dlu, p, 3dlu, p"); // rows
		
		// Specify that columns 1 & 5 as well as 3 & 7 have equal widths.       
		layout.setColumnGroups(new int[][]{{1, 5}, {3, 7}});

		PanelBuilder builder = new PanelBuilder(layout);
		builder.setDefaultDialogBorder();
		CellConstraints cc = new CellConstraints();
		int r = 1;
		int c = 1;
		
		builder.addLabel("ID", cc.xy(c, r));
		builder.add(new JTextField(), cc.xy(c + 2, r));
		r += 2;
		
		builder.addLabel("Name", cc.xy(c, r));
		builder.add(new JTextField(), cc.xy(c + 2, r));
		r += 2;
		
		builder.addLabel("Description", cc.xy(c, r));
		builder.add(createArea(true, 30, new Dimension(100, 100)), cc.xy(c + 2, r));
		r += 2;
		
		builder.addLabel("Value", cc.xy(c, r));
		builder.add(new JTextField(), cc.xy(c + 2, r));
		r += 2;
		
		c = 5;
		r = 1;
		
		Type aug;
		String[] petStrings = { "Bird", "Cat", "Dog", "Rabbit", "Pig" };

		//Create the combo box, select item at index 4.
		//Indices start at 0, so 4 specifies the pig.
		JComboBox<String> typeBox = new JComboBox<String>(petStrings);
		//typeBox.addActionListener(this);
		
		builder.addLabel("Type", cc.xy(c, r));
		builder.add(typeBox, cc.xy(c + 2, r));
		r += 2;
		
		builder.addLabel("Subtype", cc.xy(c, r));
		builder.add(new JTextField(), cc.xy(c + 2, r));
		r += 2;
		
		builder.add(new JButton("Save"), cc.xy(c + 4, 9));

		add(builder.getPanel());
		
		setPreferredSize(new Dimension(950, 500));
	}

	private JTextArea createArea(boolean lineWrap, int columns, Dimension minimumSize) {
		JTextArea area = new JTextArea();
		area.setBorder(new CompoundBorder(new LineBorder(Color.GRAY),
				new EmptyBorder(1, 3, 1, 1)));
		area.setLineWrap(lineWrap);
		area.setWrapStyleWord(true);
		area.setColumns(columns);
		if (minimumSize != null) {
			area.setMinimumSize(new Dimension(100, 32));
		}
		return area;
	}
}
