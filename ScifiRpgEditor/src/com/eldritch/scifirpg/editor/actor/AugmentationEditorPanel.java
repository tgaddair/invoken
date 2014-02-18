package com.eldritch.scifirpg.editor.actor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class AugmentationEditorPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	public AugmentationEditorPanel() {
		super(new BorderLayout());

		FormLayout layout = new FormLayout(
				"4dlu, right:p, 4dlu, fill:default:grow, 4dlu, p", // columns
				"3dlu, p, 3dlu, p, 3dlu, fill:default:grow, 3dlu, p, 3dlu, p"); // rows

		PanelBuilder builder = new PanelBuilder(layout);
		CellConstraints cc = new CellConstraints();
		int r = 2;
		int c = 2;
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
		
		builder.add(new JButton("Save"), cc.xy(c + 4, r));

		add(builder.getPanel());
		
		setPreferredSize(new Dimension(800, 800));
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
