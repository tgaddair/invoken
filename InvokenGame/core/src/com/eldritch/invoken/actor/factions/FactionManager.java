package com.eldritch.invoken.actor.factions;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.eldritch.invoken.actor.Agent;

public class FactionManager {
	private final Map<Faction, FactionStatus> factions = new HashMap<Faction, FactionStatus>();
	private final Agent agent;
	
	public FactionManager(Agent agent) {
		this.agent = agent;
	}
	
	public void addFaction(Faction faction, int rank, int reputation) {
		factions.put(faction, new FactionStatus(rank, reputation));
	}
	
	public Set<Faction> getFactions() {
		return factions.keySet();
	}
	
	public int getReputation(Faction faction) {
		int rep = 0;
		if (factions.containsKey(faction)) {
			// cap positive rep bonus at just under one rank's worth, don't limit negative rep
			FactionStatus status = factions.get(faction);
			rep += status.rank * 10 + Math.min(status.reputation, 9);
		}
		return rep;
	}
	
	public float getDisposition(Agent other) {
		float reaction = 0;
		for (Faction faction : getFactions()) {
			for (Faction otherFaction : other.getFactions()) {
				if (faction.hasRelation(otherFaction)) {
					float a = getReputation(faction) / 10f;
					float b = other.getReputation(faction) / 10f;
					float r = (a + b) * faction.getRelation(otherFaction);
					reaction += r;
				}
			}
		}
		return reaction;
	}
	
	private static class FactionStatus {
		private int rank;
		private int reputation;
		
		public FactionStatus(int rank, int reputation) {
			this.rank = rank;
			this.reputation = reputation;
		}
	}
}
