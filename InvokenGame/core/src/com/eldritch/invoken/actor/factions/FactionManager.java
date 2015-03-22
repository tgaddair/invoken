package com.eldritch.invoken.actor.factions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.proto.Actors.ActorParams;
import com.sun.istack.internal.Nullable;

public class FactionManager {
    private final Map<Faction, FactionStatus> factions = new HashMap<Faction, FactionStatus>();
    private final Agent agent;

    public FactionManager(Agent agent) {
        this.agent = agent;
    }

    /**
     * Modifies the reputation of our agent with respect to all of other's factions. After updating
     * the status, an event will be broadcast to all faction members instructing them to update
     * their disposition for our agent.
     * 
     * @param target
     *            the agent whose disposition towards us changed in some way
     * @param modifier
     *            how much to change the faction reputation
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
        for (Agent member : faction.getMembers()) {
            if (member != agent) {
                member.updateDisposition(agent);
            }
        }
    }

    public void addFaction(com.eldritch.invoken.proto.Actors.ActorParams.FactionStatus status) {
        Faction faction = Faction.forMember(agent, status.getFactionId());
        addFaction(faction, status.getRank(), status.getReputation());
    }

    public void addFaction(Faction faction, int rank, int reputation) {
        factions.put(faction, new FactionStatus(rank, reputation));
    }

    public Set<Faction> getFactions() {
        return factions.keySet();
    }
    
    @Nullable
    public Faction getDominantFaction() {
        Entry<Faction, FactionStatus> best = null;
        for (Entry<Faction, FactionStatus> entry : factions.entrySet()) {
            if (best == null || entry.getValue().rank > best.getValue().rank) {
                best = entry;
            }
        }
        return best != null ? best.getKey() : null;
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
            // only react to someone based on a faction we actually hold rank within
            if (agent.getInfo().hasRank(faction)) {
                // we only care about the sign of our reputation with this faction, whether it's
                // positive, negative, or ambivalent
                // in other words, being higher ranked in a faction should not give you a better
                // or worse opinion of others
                float a = Math.signum(getReputation(faction));

                // only consider factions we're currently on good terms with
                if (a > 0) {
                    // find a faction we have a reaction towards
                    for (Faction otherFaction : other.getInfo().getFactions()) {
                        if (faction.hasRelation(otherFaction)) {
                            // we care more about how our target is perceived by the related faction
                            float b = other.getInfo().getReputation(otherFaction) / 3f;
                            
                            // this is the modifier from this faction to the other
                            // this value is typically between [-3, 3]
                            // however, we want to rely more on individual reputation and
                            // regress reactions towards the mean to prevent treating everyone
                            // as either an extreme ally or enemy
                            float f = faction.getRelation(otherFaction);

                            // r: [-100, 100]
                            float r = a * b * f;

                            // check if they're hated by a faction we hate
                            if (b < 0 && f < 0) {
                                // scale down "the enemy of my enemy is my friend" bonus
                                r = Math.min(r, 5);
                            }

                            reaction += r;
                        }
                    }
                }
            }
        }
        
        // add 1 point of positive reaction for every 5 points in charisma
        reaction += agent.getInfo().getCharisma() / 5;
        
        return reaction;
    }

    public ActorParams.FactionStatus toProto(Faction faction) {
        FactionStatus status = getStatus(faction);
        return ActorParams.FactionStatus.newBuilder().setFactionId(faction.getId())
                .setRank(status.rank).setReputation(status.reputation).build();
    }

    private FactionStatus getStatus(Faction faction) {
        if (!factions.containsKey(faction)) {
            factions.put(faction, new FactionStatus(0, 0));
        }
        return factions.get(faction);
    }

    public static class FactionStatus {
        private int rank;
        private int reputation;

        public FactionStatus(int rank, int reputation) {
            this.rank = rank;
            this.reputation = reputation;
        }
    }
}
