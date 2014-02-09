package com.eldritch.scifirpg.view.fragment;

import com.eldritch.scifirpg.EncounterActivity;
import com.eldritch.scifirpg.model.encounters.Encounter;

public abstract class EncounterLayoutManager<T extends Encounter> {
	private final T encounter;
	private final EncounterActivity activity;
	
	public EncounterLayoutManager(T encounter, EncounterActivity activity) {
		this.encounter = encounter;
		this.activity = activity;
	}
	
	public T getEncounter() {
		return encounter;
	}
	
	public EncounterActivity getActivity() {
		return activity;
	}
	
	public abstract void loadLayout();
}
