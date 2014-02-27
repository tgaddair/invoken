package com.eldritch.scifirpg.game.view;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import com.eldritch.scifirpg.game.model.GameState;
import com.eldritch.scifirpg.game.model.LocationModel;

public class GamePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private final GameState state;
	
	public GamePanel(GameState state) {
	    super(new BorderLayout());
		this.state = state;
		
		LocationModel locationModel = state.getLocationModel();
		LocationPanel locationPanel = new LocationPanel(state, locationModel);
		locationModel.register(locationPanel);
		
		add(locationPanel);
	}
	
	public GameState getModel() {
	    return state;
	}
}
