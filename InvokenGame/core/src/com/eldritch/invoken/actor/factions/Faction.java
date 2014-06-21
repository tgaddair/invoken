package com.eldritch.invoken.actor.factions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.scifirpg.proto.Factions.Faction.Relation;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class Faction {
    private static final LoadingCache<String, Faction> factionLoader = CacheBuilder.newBuilder()
            .build(new CacheLoader<String, Faction>() {
                public Faction load(String id) {
                    com.eldritch.scifirpg.proto.Factions.Faction proto = InvokenGame.FACTION_READER
                            .readAsset(id);
                    Faction faction = new Faction(proto.getId(), proto.getName());
                    for (Relation relation : proto.getRelationList()) {
                        faction.addRelation(relation.getFactionId(), relation.getReaction());
                    }
                    return faction;
                }
            });
    
    private final String id;
    private final String name;
    private final Map<String, Integer> relations = new HashMap<String, Integer>();
    private final Set<Agent> members = new HashSet<Agent>();

    private Faction(String id, String name) {
        this.id = id;
        this.name = name;
    }
    
    public Set<Agent> getMembers() {
        return members;
    }

    public void addRelation(Faction other, int reaction) {
        addRelation(other.id, reaction);
    }

    public void addRelation(String id, int reaction) {
        relations.put(id, reaction);
    }

    public boolean hasRelation(Faction other) {
        return relations.containsKey(other.id) || other.id.equals(this.id);
    }

    public int getRelation(Faction other) {
        return getRelation(other.id);
    }

    public int getRelation(String factionId) {
        if (factionId.equals(this.id)) {
            // always have +3 reaction to members of the same faction
            return 3;
        }
        if (relations.containsKey(factionId)) {
            return relations.get(factionId);
        }
        return 0;
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Faction other = (Faction) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
    
    public static Faction forMember(Agent member, String id) {
        Faction faction = of(id);
        faction.members.add(member);
        return faction;
    }
    
    private static Faction of(String id) {
        try {
            return factionLoader.get(id);
        } catch (ExecutionException ex) {
            InvokenGame.error("Failed to load faction: " + id, ex);
            return null;
        }
    }
}
