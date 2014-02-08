package com.eldritch.scifirpg.model;

import java.util.HashMap;
import java.util.Map;


public enum Occupation {
	Gunslinger(Attribute.MIGHT, Attribute.CHARISMA, true),
	Mercenary(Attribute.INTELLECT, Attribute.MIGHT, true),
	Scholar(Attribute.WILLPOWER, Attribute.PERCEPTION, true),
	Tycoon(Attribute.DEXTERITY, Attribute.WILLPOWER, true),
	Trapper(Attribute.PERCEPTION, Attribute.INTELLECT, true),
	Bandit(Attribute.CHARISMA, Attribute.DEXTERITY, true);
	
	private final Attribute primary;
	private final Attribute secondary;
	private final boolean playable;
	
	private Occupation(Attribute primary, Attribute secondary, boolean playable) {
		this.primary = primary;
		this.secondary = secondary;
		this.playable = playable;
	}
	
	public boolean isPlayable() {
		return playable;
	}
	
	public Attribute getPrimary() {
		return primary;
	}
	
	public Attribute getSecondary() {
		return secondary;
	}
	
	public Map<Attribute, Integer> getStartingAttributes() {
		Map<Attribute, Integer> atts = new HashMap<Attribute, Integer>();
		for (Attribute att : Attribute.values()) {
			atts.put(att, 5);
		}
		atts.put(getPrimary(), 15);
		atts.put(getSecondary(), 10);
		return atts;
	}
}
