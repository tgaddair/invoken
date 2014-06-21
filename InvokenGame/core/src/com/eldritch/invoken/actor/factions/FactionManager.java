package com.eldritch.invoken.actor.factions;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.eldritch.invoken.actor.type.Agent;

public class FactionManager {
	private final Map<Faction, FactionStatus> factions = new HashMap<Faction, FactionStatus>();
	private final Agent agent;
	
	public FactionManager(Agent agent) {
		this.agent = agent;
	}
	
	public void modifyReputation(Agent other, float modifier) {
	    for (Faction faction : other.getInfo().getFactions()) {
	        if (factions.containsKey(faction)) {
	            FactionStatus status = factions.get(faction);
	            status.reputation -= modifier;
	            // TODO emit these changes to the other actors
	        }
	    }
	}
	
	public void addFaction(com.eldritch.scifirpg.proto.Actors.ActorParams.FactionStatus status) {
		Faction faction = Faction.of(status.getFactionId());
		addFaction(faction, status.getRank(), status.getReputation());
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
		// special case for followers
		if (agent.isFollowing(other)) {
			return 100;
		}
		
		// calculate disposition from factions
		float reaction = 0;
		for (Faction faction : getFactions()) {
			for (Faction otherFaction : other.getInfo().getFactions()) {
				if (faction.hasRelation(otherFaction)) {
					float a = getReputation(faction) / 10f;
					float b = other.getInfo().getReputation(otherFaction) / 5f;
					float r = (a + b) * faction.getRelation(otherFaction);
					reaction += r;
				}
			}
		}
		return reaction;
	}
	
	private static class FactionStatus {
		private int rank;
		private float reputation;
		
		public FactionStatus(int rank, int reputation) {
			this.rank = rank;
			this.reputation = reputation;
		}
	}
}
