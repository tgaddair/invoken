package com.eldritch.invoken.actor.ai;

import java.util.List;

import com.eldritch.invoken.actor.factions.Faction;
import com.eldritch.invoken.actor.type.Npc;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;

public class Squad {
    private final Faction faction;
    private final List<Npc> members = Lists.newArrayList();
    private final Optional<Npc> leader;
    
    public Squad(Faction faction, List<Npc> members) {
        this.faction = faction;
        this.members.addAll(members);
        
        Npc best = null;
        for (Npc npc : members) {
            if (best == null || npc.getInfo().getRank(faction) > best.getInfo().getRank(faction)) {
                best = npc;
            }
        }
        this.leader = Optional.fromNullable(best);
    }
}
