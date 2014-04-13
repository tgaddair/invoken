package com.eldritch.invoken.actor.factions;

import java.util.HashMap;
import java.util.Map;

public class Faction {
	private final Map<String, Integer> relations = new HashMap<String, Integer>();
	private final String name;
	
	public Faction(String name) {
		this.name = name;
	}
	
	public void addRelation(Faction other, int reaction) {
		relations.put(other.name, reaction);
	}
	
	public boolean hasRelation(Faction other) {
		return relations.containsKey(other.name);
	}
	
	public int getRelation(Faction other) {
		return getRelation(other.name);
	}
	
	public int getRelation(String factionId) {
		if (relations.containsKey(factionId)) {
			return relations.get(factionId);
		}
		return 0;
	}
	
	public String getName() {
		return name;
	}
}
