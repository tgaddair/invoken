package com.eldritch.invoken.actor.factions;

import java.util.HashMap;
import java.util.Map;

import com.eldritch.scifirpg.proto.Factions.Faction.Relation;

public class Faction {
	private final String id;
	private final String name;
	private final Map<String, Integer> relations = new HashMap<String, Integer>();
	
	public Faction(String name) {
		this(name, name);
	}
	
	public Faction(String id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public void addRelation(Faction other, int reaction) {
		addRelation(other.id, reaction);
	}
	
	public void addRelation(String id, int reaction) {
		relations.put(id, reaction);
	}
	
	public boolean hasRelation(Faction other) {
		return relations.containsKey(other.id);
	}
	
	public int getRelation(Faction other) {
		return getRelation(other.id);
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
	
	public static Faction fromProto(com.eldritch.scifirpg.proto.Factions.Faction proto) {
		Faction faction = new Faction(proto.getId(), proto.getName());
		for (Relation relation : proto.getRelationList()) {
			faction.addRelation(relation.getFactionId(), relation.getReaction());
		}
		return faction;
	}
}
