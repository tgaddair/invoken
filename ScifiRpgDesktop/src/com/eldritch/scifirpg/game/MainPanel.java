package com.eldritch.scifirpg.game;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;

import com.eldritch.scifirpg.game.model.GameState;
import com.eldritch.scifirpg.game.view.GamePanel;
import com.eldritch.scifirpg.game.view.ProfessionPanel;
import com.eldritch.scifirpg.proto.Disciplines.Profession;

public class MainPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private final GamePanel gamePanel;
	
	public MainPanel() {
	    super(new BorderLayout());
	    this.gamePanel = new GamePanel(new GameState(Profession.CENTURION));
	            
		//add(new ProfessionPanel());
	    add(gamePanel);
		setPreferredSize(new Dimension(450, 800));
	}
	
	public GamePanel getGamePanel() {
	    return gamePanel;
	}
}
