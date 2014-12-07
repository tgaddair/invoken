package com.eldritch.scifirpg.game;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.eldritch.scifirpg.game.model.GameState;
import com.eldritch.scifirpg.game.view.GamePanel;
import com.eldritch.invoken.proto.Disciplines.Profession;

public class MainPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private final GamePanel gamePanel;
	private final GameOverPanel overPanel = new GameOverPanel();
	
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
	
	public GameOverPanel getGameOverPanel() {
	    return overPanel;
	}
	
	public static class GameOverPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        
        public GameOverPanel() {
            super(new BorderLayout());
            
            JLabel title = new JLabel("Game Over!");
            title.setFont(title.getFont().deriveFont(24.0f));
            title.setHorizontalAlignment(SwingConstants.CENTER);
            
            add(title);
        }
	    
	}
}
