package com.eldritch.scifirpg.game.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.eldritch.scifirpg.game.Application;
import com.eldritch.scifirpg.game.model.GameState;
import com.eldritch.scifirpg.game.util.ProfessionUtil;
import com.eldritch.scifirpg.proto.Disciplines.Discipline;
import com.eldritch.scifirpg.proto.Disciplines.Profession;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class ProfessionPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	
	private final JComboBox<Discipline> discipline1 = new JComboBox<Discipline>(Discipline.values());
	private final JComboBox<Discipline> discipline2 = new JComboBox<Discipline>(Discipline.values());
	private final JLabel profLabel = new JLabel();
	private Profession profession;

	public ProfessionPanel() {
		DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(""));
		builder.border(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		builder.appendColumn("right:pref");
		builder.appendColumn("3dlu");
		builder.appendColumn("fill:default:grow");
		
		final JButton acceptButton = new JButton("Accept");
		acceptButton.setEnabled(false);
		
		List<Discipline> values = new ArrayList<>();
		values.add(null);
		values.addAll(Arrays.asList(Discipline.values()));
		discipline1.setModel(new DefaultComboBoxModel<Discipline>(values.toArray(new Discipline[0])));
		discipline1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Discipline d1 = (Discipline) discipline1.getSelectedItem();
				discipline2.setEnabled(d1 != null);
				if (d1 == null) {
					acceptButton.setEnabled(false);
					profLabel.setText("");
				}
			}
		});
		builder.append("First Discipline:", discipline1);
		builder.nextLine();
		
		values = new ArrayList<>();
		values.add(null);
		values.addAll(Arrays.asList(Discipline.values()));
		discipline2.setModel(new DefaultComboBoxModel<Discipline>(values.toArray(new Discipline[0])));
		discipline2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Discipline d2 = (Discipline) discipline2.getSelectedItem();
				acceptButton.setEnabled(d2 != null);
				
				if (d2 != null) {
					Discipline d1 = (Discipline) discipline1.getSelectedItem();
					Profession p = ProfessionUtil.getProfessionFor(d1, d2);
					profLabel.setText(p.name());
					profession = p;
				} else {
					profLabel.setText("");
				}
			}
		});
		discipline2.setEnabled(false);
		builder.append("Second Discipline:", discipline2);
		builder.nextLine();
		
		builder.append("Profession:", profLabel);
		builder.nextLine();
		
		acceptButton.addActionListener(this);
		builder.append(acceptButton);
		builder.nextLine();
		
		add(builder.getPanel());
	}

	@Override
	public void actionPerformed(ActionEvent ev) {
	    GameState gameState = new GameState(profession);
	    Application.getApplication().setPanel(new GamePanel(gameState));
	}
}
