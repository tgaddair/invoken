package com.eldritch.scifirpg.game.view;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.eldritch.scifirpg.game.Application;
import com.eldritch.scifirpg.game.model.GameState;

public class GamePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private final GameState model;
	
	public GamePanel(GameState model) {
	    super(new BorderLayout());
		this.model = model;
		add(new LocationPanel(model.getLocationModel()));
	}
	
	public void reloadLocation() {
	    SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                removeAll();
                add(new LocationPanel(model.getLocationModel()));
                Application.getApplication().getFrame().revalidate();
            }
        });
	}
	
	public GameState getModel() {
	    return model;
	}
}
