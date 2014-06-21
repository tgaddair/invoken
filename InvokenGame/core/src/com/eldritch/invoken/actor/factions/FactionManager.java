package com.eldritch.invoken.actor.factions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.eldritch.invoken.actor.type.Agent;

public class FactionManager {
	private final Map<Faction, FactionStatus> factions = new HashMap<Faction, FactionStatus>();
	private final Agent agent;
	
	public FactionManager(Agent agent) {
		this.agent = agent;
	}
	
	/**
	 * Modifies the reputation of our agent with respect to all of other's factions.  After
	 * updating the status, an event will be broadcast to all faction members instructing them to
	 * update their disposition for our agent.
	 * 
	 * @param target the agent whose disposition towards us changed in some way
	 * @param modifier how much to change the faction reputation
	 */
	public void modifyReputationFor(Agent target, float modifier) {
	    Set<Agent> comrades = new HashSet<Agent>();
	    for (Faction faction : target.getInfo().getFactions()) {
	        if (target.getInfo().hasRank(faction)) {
	            // only modify if we actually hold a rank in this faction
                FactionStatus status = getStatus(faction);
                status.reputation += modifier;
                comrades.addAll(faction.getMembers());
	        }
	    }
	    
	    // update the disposition towards this agent for all our target's comrades
	    comrades.remove(agent); // don't update ourselves
	    for (Agent comrade : comrades) {
	        comrade.updateDisposition(agent);
	    }
	}
	
	public void modifyReputationFor(Faction faction, float modifier) {
        FactionStatus status = getStatus(faction);
        status.reputation += modifier;
        
        // notify all faction members
        Set<Agent> members = new HashSet<Agent>();
        members.addAll(faction.getMembers());
        members.remove(agent);
        for (Agent member : members) {
            member.updateDisposition(agent);
        }
	}
	
	public void addFaction(com.eldritch.scifirpg.proto.Actors.ActorParams.FactionStatus status) {
		Faction faction = Faction.forMember(agent, status.getFactionId());
		addFaction(faction, status.getRank(), status.getReputation());
	}
	
	public void addFaction(Faction faction, int rank, int reputation) {
		factions.put(faction, new FactionStatus(rank, reputation));
	}
	
	public Set<Faction> getFactions() {
		return factions.keySet();
	}
	
	public int getRank(Faction faction) {
	    if (factions.containsKey(faction)) {
	        return factions.get(faction).rank;
	    }
	    return 0;
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
	
	public FactionStatus getStatus(Faction faction) {
	    if (!factions.containsKey(faction)) {
	        factions.put(faction, new FactionStatus(0, 0));
	    }
	    return factions.get(faction);
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
