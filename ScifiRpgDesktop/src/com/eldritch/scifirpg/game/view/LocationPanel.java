package com.eldritch.scifirpg.game.view;

import javax.swing.JPanel;

import com.eldritch.scifirpg.game.model.LocationModel;
import com.eldritch.scifirpg.game.model.RegionEncounter;

public class LocationPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private final LocationModel model;
	
	public LocationPanel(LocationModel model) {
		this.model = model;
		add(new RegionEncounterPanel((RegionEncounter) model.drawEncounter()));
	}
}
