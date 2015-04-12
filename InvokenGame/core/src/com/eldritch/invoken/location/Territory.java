package com.eldritch.invoken.location;

import com.eldritch.invoken.actor.factions.Faction;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.proto.Locations;
import com.google.common.base.Optional;
import com.google.common.base.Strings;

public class Territory {
    public static final Territory DEFAULT = new Territory(Locations.Territory.getDefaultInstance());
    
    private final Optional<Faction> owningFaction;
    private final int minRank;
    private final Optional<String> credential;

    public Territory(Locations.Territory proto) {
        owningFaction = Optional.fromNullable(proto.hasFactionId() ? Faction.of(proto
                .getFactionId()) : null);
        minRank = owningFaction.isPresent() ? proto.getMinRank() : 0;
        credential = !Strings.isNullOrEmpty(proto.getCredential()) ? Optional.of(proto
                .getCredential()) : Optional.<String> absent();
    }

    public void alertTo(Agent intruder) {
        if (owningFaction.isPresent()) {
            Faction faction = owningFaction.get();
            intruder.changeFactionStatus(faction, -50);
        }
    }

    public boolean isTrespasser(Agent agent) {
        if (!owningFaction.isPresent()) {
            // no owning faction
            return true;
        }

        if (credential.isPresent()) {
            boolean hasCredential = agent.getInventory().hasItem(credential.get());
            if (hasCredential) {
                // let them through
                return false;
            }
        }

        Faction faction = owningFaction.get();
        int rank = agent.getInfo().getFactionManager().getRank(faction);
        return rank < minRank;
    }
}
