package com.eldritch.scifirpg.game;

import java.awt.BorderLayout;

import javax.swing.JFrame;

import com.eldritch.scifirpg.game.MainPanel;

public class Application {
	private static void createAndShowGUI() {
        // Create and set up the window.
        JFrame frame = new JFrame("SciFi RPG: The Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
        // Add content to the window.
        frame.add(new MainPanel(), BorderLayout.CENTER);
        
        // Display the window.
        frame.pack();
        frame.setVisible(true);
    }
	
	public static void main(String[] args) {
		// Schedule a job for the event-dispatching thread:
        // creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
	}
}
