package com.eldritch.scifirpg.model;

import java.util.ArrayList;
import java.util.List;

public class EncounterModel {
	private final List<Encounter> encounters;
	private final EncounterState state;
	
	public EncounterModel() {
		encounters = new ArrayList<Encounter>();
		state = new EncounterState(GameSettings.getGame().getPlayer());
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
	
	public void setSneaking(boolean sneaking) {
		state.setSneaking(sneaking);
	}
	
	public static class EncounterState {
		private final Person player;
		private boolean sneaking;
		
		public EncounterState(Person player) {
			this.player = player;
			sneaking = false;
		}

		public boolean isSneaking() {
			return sneaking;
		}

		public void setSneaking(boolean sneaking) {
			this.sneaking = sneaking;
		}

		public Person getPlayer() {
			return player;
		}
	}
}
