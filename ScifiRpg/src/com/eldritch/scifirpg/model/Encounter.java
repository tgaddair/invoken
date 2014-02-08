package com.eldritch.scifirpg.model;

public class Encounter {
	private final double weight;
	private final boolean unique;
	
	public Encounter() {
		this(1.0, false);
	}
	
	public Encounter(double weight) {
		this(weight, false);
	}
	
	public Encounter(double weight, boolean unique) {
		this.weight = weight;
		this.unique = unique;
	}

	public boolean isUnique() {
		return unique;
	}

	public double getWeight() {
		return weight;
	}
}
