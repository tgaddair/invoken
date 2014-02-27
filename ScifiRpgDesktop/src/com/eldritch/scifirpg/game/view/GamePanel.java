package com.eldritch.scifirpg.game.view;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import com.eldritch.scifirpg.game.model.GameState;
import com.eldritch.scifirpg.game.model.LocationModel;

public class GamePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private final GameState model;
	
	public GamePanel(GameState model) {
	    super(new BorderLayout());
		this.model = model;
		
		LocationModel locationModel = model.getLocationModel();
		LocationPanel locationPanel = new LocationPanel(locationModel);
		locationModel.register(locationPanel);
		
		add(locationPanel);
	}
	
	public GameState getModel() {
	    return model;
	}
}
