package com.eldritch.scifirpg.model;

import java.util.Map;

public class Person {
	private final String firstName;
	private final String lastName;
	private final String alias;
	private final Gender gender;
	private final Occupation occupation;
	private final Map<Attribute, Integer> atts;
	private int level;
	private int health;
	private int intoxication;
	private int actions;
	
	public Person(String firstName, String lastName, String alias,
			Gender gender, Occupation occupation) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.alias = alias;
		this.gender = gender;
		this.occupation = occupation;
		atts = occupation.getStartingAttributes();
		
		level = 1;
		health = get(Attribute.INTELLECT);
		intoxication = 0;
		actions = 50;
	}

	public void modifyHealth(int delta) {
		health += delta;
	}
	
	public int getHealth() {
		return health;
	}
	
	public boolean hasHealth() {
		return health > 0;
	}
	
	public void modifyIntoxication(int delta) {
		intoxication += delta;
	}

	public int getIntoxication() {
		return intoxication;
	}
	
	public boolean hasIntoxication() {
		return intoxication > 0;
	}
	
	public void modifyActions(int delta) {
		actions += delta;
	}

	public int getActions() {
		return actions;
	}
	
	public boolean hasActions() {
		return actions > 0;
	}

	public void levelUp(Map<Attribute, Integer> delta) {
		atts.put(occupation.getPrimary(), atts.get(occupation.getPrimary()) + 3);
		atts.put(occupation.getSecondary(), atts.get(occupation.getSecondary()) + 2);
		for (Attribute att : delta.keySet()) {
			atts.put(att, atts.get(att) + delta.get(att));
		}
		level++;
	}
	
	public int get(Attribute att) {
		return atts.get(att);
	}
	
	public int getLevel() {
		return level;
	}
	
	public String getFullName() {
		return firstName + " " + lastName;
	}
	
	public String getCompleteName() {
		return String.format("%s \"%s\" %s", firstName, alias, lastName);
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}
	
	public String getAlias() {
		return alias;
	}

	public Gender getGender() {
		return gender;
	}

	public Occupation getOccupation() {
		return occupation;
	}

	public enum Gender {
		MALE, FEMALE
	}
}
