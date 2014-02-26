package com.eldritch.scifirpg.game.view;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.eldritch.scifirpg.game.model.GameState;
import com.eldritch.scifirpg.proto.Locations.Location;

public class GamePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private final GameState model;
	
	public GamePanel(GameState model) {
		this.model = model;
		add(new LocationPanel(model.getLocationModel()));
	}
	
	public void reloadLocation() {
	    SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                removeAll();
                add(new LocationPanel(model.getLocationModel()));
            }
        });
	}
	
	public GameState getModel() {
	    return model;
	}
}
