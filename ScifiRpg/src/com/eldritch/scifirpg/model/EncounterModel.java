package com.eldritch.scifirpg.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.eldritch.scifirpg.model.encounters.Encounter;

public class EncounterModel {
	private final List<Encounter> encounters;
	
	public EncounterModel(Collection<Encounter> encounters) {
		this.encounters = new ArrayList<Encounter>(encounters);
	}
	
	public void add(Encounter enc) {
		encounters.add(enc);
	}
	
	public Encounter draw() {
		double total = 0.0;
		for (Encounter enc : encounters) {
			total += enc.getWeight();
		}
		
		double seed = Math.random() * total;
		double sum = 0.0;
		for (Encounter enc : encounters) {
			sum += enc.getWeight();
			if (seed <= sum) {
				return enc;
			}
		}
		
		return null;
	}
}
