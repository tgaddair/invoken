package com.eldritch.scifirpg.game.view;

import javax.swing.JPanel;

import com.eldritch.scifirpg.game.model.GameState;

public class GamePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private final GameState model;
	
	public GamePanel(GameState model) {
		this.model = model;
		add(new LocationPanel(model.getLocation()));
	}
}
