package com.eldritch.scifirpg.model.encounters;

import com.eldritch.scifirpg.model.Resolution;

public class StaticEncounter extends Encounter {
	private final String title;
	private final String description;
	private final Resolution resoltion;
	
	public StaticEncounter(String title, String description, Resolution resolution, double weight, boolean unique) {
		super(weight, unique);
		this.title = title;
		this.description = description;
		this.resoltion = resolution;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public Resolution getResoltion() {
		return resoltion;
	}
}
