package com.eldritch.scifirpg.model.locations;

import com.eldritch.scifirpg.model.EncounterModel;

public class ClosedLocation extends Location {
	private final OpenLocation parent;
	private final EncounterModel eModel;
	
	public ClosedLocation(String name, int actions, OpenLocation parent) {
		super(name, actions);
		this.parent = parent;
		eModel = new EncounterModel();
	}
}
