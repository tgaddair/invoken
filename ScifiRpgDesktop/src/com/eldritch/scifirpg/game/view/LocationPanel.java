package com.eldritch.scifirpg.game.view;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import com.eldritch.scifirpg.game.model.AbstractEncounter;
import com.eldritch.scifirpg.game.model.DecisionEncounter;
import com.eldritch.scifirpg.game.model.LocationModel;
import com.eldritch.scifirpg.game.model.RegionEncounter;
import com.eldritch.scifirpg.game.model.StaticEncounter;

public class LocationPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private final LocationModel model;
	
	public LocationPanel(LocationModel model) {
	    super(new BorderLayout());
		this.model = model;
		
		AbstractEncounter encounter = model.drawEncounter();
		JPanel encounterPanel = null;
		switch (encounter.getType()) {
		    case STATIC:
		        encounterPanel = new StaticEncounterPanel((StaticEncounter) model.drawEncounter());
		        break;
		    case DECISION:
		        //encounterPanel = new DecisionEncounterPanel((DecisionEncounter) model.drawEncounter());
		        break;
		    case ACTOR:
		        //encounterPanel = new ActorEncounterPanel((ActorEncounter) model.drawEncounter());
		        break;
		    case REGION:
		        encounterPanel = new RegionEncounterPanel((RegionEncounter) model.drawEncounter());
		        break;
	        default:
	            throw new IllegalArgumentException(
                        "Unrecognized Encounter type " + encounter.getType());
		}
		add(new EncounterPanel(encounter, model, encounterPanel));
	}
}
