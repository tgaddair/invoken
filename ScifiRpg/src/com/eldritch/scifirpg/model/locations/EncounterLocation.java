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
		private Location parent;
		private EncounterModel eModel;
		
		private Builder() {
		}
		
		public String getName() {
			return name;
		}

		public Builder setName(String name) {
			this.name = name;
			return this;
		}

		public Location getParent() {
			return parent;
		}

		public Builder setParent(Location parent) {
			this.parent = parent;
			return this;
		}

		public EncounterModel getEncounterModel() {
			return eModel;
		}

		public Builder setEncounterModel(EncounterModel eModel) {
			this.eModel = eModel;
			return this;
		}

		public EncounterLocation build() {
			return new EncounterLocation(name, parent, eModel);
		}
	}
	
	public static Builder newBuilder() {
		return new Builder();
	}
}
