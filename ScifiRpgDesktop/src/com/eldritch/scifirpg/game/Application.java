package com.eldritch.scifirpg.game;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.eldritch.scifirpg.game.MainPanel;
import com.eldritch.scifirpg.game.model.GameState;
import com.eldritch.scifirpg.game.view.GamePanel;

public class Application {
    private final JFrame frame;
    private final MainPanel mainPanel;
	
	private Application() {
	    // Create and set up the window.
        frame = new JFrame("SciFi RPG: The Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
	    mainPanel = new MainPanel();
	}
	
	public void setPanel(final JPanel panel) {
	    SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                mainPanel.removeAll();
                mainPanel.add(panel);
                frame.revalidate();
            }
	    });
	}
	
	public GamePanel getGamePanel() {
	    return mainPanel.getGamePanel();
	}
	
	public MainPanel getMainPanel() {
		return mainPanel;
	}
	
	public JFrame getFrame() {
	    return frame;
	}
	
	private void createAndShowGUI() {
        // Add content to the window.
        frame.add(mainPanel, BorderLayout.CENTER);
        
        // Display the window.
        frame.pack();
        frame.setVisible(true);
    }
	
	public static void main(String[] args) {
		// Schedule a job for the event-dispatching thread:
        // creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                getApplication().createAndShowGUI();
            }
        });
	}
	
	public static GameState getGameState() {
	    return getApplication().getGamePanel().getModel();
	}
	
	public static Application getApplication() {
		return AppHolder.APP;
	}
	
	private static class AppHolder {
		private static final Application APP = new Application();
	}
}
