package com.eldritch.scifirpg.game;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.eldritch.scifirpg.game.MainPanel;

public class Application {
private final MainPanel panel;
	
	private Application() {
		panel = new MainPanel();
	}
	
	public void setPanel(JPanel panel) {
		panel.removeAll();
		panel.add(panel);
	}
	
	public MainPanel getMainPanel() {
		return panel;
	}
	
	private void createAndShowGUI() {
        // Create and set up the window.
        JFrame frame = new JFrame("SciFi RPG: The Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
        // Add content to the window.
        frame.add(panel, BorderLayout.CENTER);
        
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
	
	public static Application getApplication() {
		return AppHolder.APP;
	}
	
	private static class AppHolder {
		private static final Application APP = new Application();
	}
}
