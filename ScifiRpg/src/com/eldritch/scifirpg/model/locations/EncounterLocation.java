package com.eldritch.scifirpg.model.locations;

import com.eldritch.scifirpg.model.EncounterModel;

public class EncounterLocation extends Location {
	private final OpenLocation parent;
	private final EncounterModel eModel;
	
	public EncounterLocation(String name, int actions, OpenLocation parent) {
		super(name, actions);
		this.parent = parent;
		eModel = new EncounterModel();
	}
}
