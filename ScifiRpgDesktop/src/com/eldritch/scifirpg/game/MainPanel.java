package com.eldritch.scifirpg.game;

import java.awt.Dimension;

import javax.swing.JPanel;

import com.eldritch.scifirpg.game.view.ProfessionPanel;

public class MainPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	public MainPanel() {
		add(new ProfessionPanel());
		setPreferredSize(new Dimension(450, 800));
	}
}
