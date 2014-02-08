package com.eldritch.scifirpg.model.aug;

import com.eldritch.scifirpg.model.Mastery;

public abstract class Augmentation {
	private final int slots;
	private final Mastery mastery;
	private final int requiredAttribute;
	
	public Augmentation(int slots, Mastery mastery, int requiredAttribute) {
		this.slots = slots;
		this.mastery = mastery;
		this.requiredAttribute = requiredAttribute;
	}
}
