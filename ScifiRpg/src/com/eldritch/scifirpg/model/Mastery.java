package com.eldritch.scifirpg.model;

public enum Mastery {
	MELEE(Attribute.MIGHT),
	DEFENSE(Attribute.MIGHT),
	RANGED(Attribute.DEXTERITY),
	EVASION(Attribute.DEXTERITY),
	RESEARCH(Attribute.INTELLECT),
	HACKING(Attribute.INTELLECT),
	PSIONICS(Attribute.WILLPOWER),
	LEADERSHIP(Attribute.WILLPOWER),
	STEALTH(Attribute.PERCEPTION),
	DISCOVERY(Attribute.PERCEPTION),
	PERSUASION(Attribute.CHARISMA),
	SEDUCTION(Attribute.CHARISMA);
	
	private final Attribute governingAttribute;
	
	private Mastery(Attribute governingAttribute) {
		this.governingAttribute = governingAttribute;
	}
}
