package com.eldritch.scifirpg.model.locations;

import com.eldritch.scifirpg.model.Encounter;
import com.eldritch.scifirpg.model.EncounterModel;

public class EncounterLocation extends Location {
	private final EncounterModel eModel;
	
	/**
	 * Constructs an EncounterLocation
	 * 
	 * @param name The location's display name.
	 * @param parent An {@link EncounterLocation} must have a parent to exit into.
	 * @param eModel The {@link EncounterModel} used to select the next {@link Encounter}.
	 */
	public EncounterLocation(String name, Location parent, EncounterModel eModel) {
		super(name, parent);
		this.eModel = eModel;
	}
	
	public Encounter nextEncounter() {
		return eModel.draw();
	}
	
	public static class Builder {
		private String name;
		private OverworldLocation parent;
		private EncounterModel eModel;
		
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public OverworldLocation getParent() {
			return parent;
		}

		public void setParent(OverworldLocation parent) {
			this.parent = parent;
		}

		public EncounterModel geteModel() {
			return eModel;
		}

		public void seteModel(EncounterModel eModel) {
			this.eModel = eModel;
		}

		public EncounterLocation build() {
			return new EncounterLocation(name, parent, eModel);
		}
	}
}
