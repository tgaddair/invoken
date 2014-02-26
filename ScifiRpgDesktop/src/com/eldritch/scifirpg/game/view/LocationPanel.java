package com.eldritch.scifirpg.game.view;

import javax.swing.JPanel;

import com.eldritch.scifirpg.game.model.AbstractEncounter;
import com.eldritch.scifirpg.game.model.LocationModel;
import com.eldritch.scifirpg.game.model.RegionEncounter;
import com.eldritch.scifirpg.game.model.StaticEncounter;

public class LocationPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private final LocationModel model;
	
	public LocationPanel(LocationModel model) {
		this.model = model;
		
		AbstractEncounter encounter = model.drawEncounter();
		switch (encounter.getType()) {
		    case STATIC:
		        add(new StaticEncounterPanel((StaticEncounter) model.drawEncounter()));
		        break;
		    case DECISION:
		        break;
		    case ACTOR:
		        break;
		    case REGION:
		        add(new RegionEncounterPanel((RegionEncounter) model.drawEncounter()));
		        break;
	        default:
	            throw new IllegalArgumentException(
                        "Unrecognized Encounter type " + encounter.getType());
		}
	}
}
