package com.eldritch.invoken.actor.ai;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.eldritch.invoken.actor.factions.Faction;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Npc;
import com.google.common.base.Preconditions;

public class Squad {
    private final Faction faction;
    private final Set<Npc> members = new HashSet<>();
    private final Npc leader;
    
    public Squad(Faction faction, List<Npc> members) {
        this.faction = faction;
        this.members.addAll(members);
        
        // as defined, leader cannot be null, as a squad must have at least one member
        Preconditions.checkArgument(!members.isEmpty(), "Squads must have at least one member");
        Npc best = null;
        for (Npc npc : members) {
            if (best == null || npc.getInfo().getRank(faction) > best.getInfo().getRank(faction)) {
                best = npc;
            }
        }
        this.leader = best;
    }
    
    public boolean contains(Agent agent) {
        return members.contains(agent);
    }
    
    public Npc getLeader() {
        return leader;
    }
}
