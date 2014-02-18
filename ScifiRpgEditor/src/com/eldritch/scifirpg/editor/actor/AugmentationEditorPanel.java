package com.eldritch.scifirpg.editor.actor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.eldritch.scifirpg.proto.Augmentations.Augmentation.AttackSubtype;
import com.eldritch.scifirpg.proto.Augmentations.Augmentation.DeceiveSubtype;
import com.eldritch.scifirpg.proto.Augmentations.Augmentation.ExecuteSubtype;
import com.eldritch.scifirpg.proto.Augmentations.Augmentation.Type;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class AugmentationEditorPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private final JTextField idField = new JTextField();
	private final JTextField nameField = new JTextField();
	private final JComboBox<Type> typeBox = new JComboBox<Type>(Type.values());
	private final JComboBox<Enum<?>> subtypeBox = new JComboBox<Enum<?>>(AttackSubtype.values());

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
		
		nameField.addActionListener(new NameTypedListener());
		builder.addLabel("Name", cc.xy(c, r));
		builder.add(nameField, cc.xy(c + 2, r));
		r += 2;
		
		builder.addLabel("ID", cc.xy(c, r));
		builder.add(idField, cc.xy(c + 2, r));
		r += 2;
		
		builder.addLabel("Description", cc.xy(c, r));
		builder.add(createArea(true, 30, new Dimension(100, 100)), cc.xy(c + 2, r));
		r += 2;
		
		builder.addLabel("Value", cc.xy(c, r));
		builder.add(new JTextField(), cc.xy(c + 2, r));
		r += 2;
		
		c = 5;
		r = 1;
		
		typeBox.addActionListener(new TypeSelectionListener());
		builder.addLabel("Type", cc.xy(c, r));
		builder.add(typeBox, cc.xy(c + 2, r));
		r += 2;
		
		builder.addLabel("Subtype", cc.xy(c, r));
		builder.add(subtypeBox, cc.xy(c + 2, r));
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
	
	private class NameTypedListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			JTextField source = (JTextField) e.getSource();
			idField.setText(source.getText().replaceAll(" ", ""));
		}
	}
	
	private class TypeSelectionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			Enum<?>[] values = null;
			boolean visible = true;
			
			Type t = (Type) typeBox.getSelectedItem();
			switch (t) {
				case ATTACK:
				case COUNTER:
					values = AttackSubtype.values();
					break;
				case DECEIVE:
				case REVEAL:
					values = DeceiveSubtype.values();
					break;
				case EXECUTE:
				case INTERRUPT:
					values = ExecuteSubtype.values();
					break;
				case DIALOGUE:
				case PASSIVE:
					visible = false;
					values = new Enum<?>[0];
					break;
				default:
					throw new IllegalStateException("Unrecognized Augmentation Type: " + t);
			}
			
			subtypeBox.setVisible(visible);
			subtypeBox.setModel(new DefaultComboBoxModel<Enum<?>>(values));
		}
	}
}
