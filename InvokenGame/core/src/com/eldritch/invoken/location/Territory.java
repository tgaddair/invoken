package com.eldritch.invoken.location;

import java.util.HashMap;
import java.util.Map;

import com.eldritch.invoken.actor.factions.Faction;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.proto.Locations;
import com.google.common.base.Optional;
import com.google.common.base.Strings;

public class Territory {
    public static final Territory DEFAULT = new Territory(Optional.<Faction> absent(),
            Locations.Territory.getDefaultInstance());

    private final Map<ConnectedRoom, Boolean> frontier = new HashMap<>();
    private final Optional<Faction> owningFaction;
    private final int minRank;
    private final Optional<String> credential;

    public Territory(Optional<Faction> faction, Locations.Territory proto) {
        owningFaction = faction;
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
            // no owning faction, so cannot trespass here
            return false;
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

    public boolean isOnFrontier(ConnectedRoom room) {
        // assumes we're trespassing, are we trespassing enough for it to be a problem?
        if (room == null) {
            // no violation
            return true;
        }
        return isFrontier(room);
    }

    private boolean isFrontier(ConnectedRoom room) {
        if (!owningFaction.isPresent() || !room.getFaction().isPresent()
                || !owningFaction.get().getId().equals(room.getFaction().get())) {
            // we have no frontier, or different faction
            return true;
        }

        if (!frontier.containsKey(room)) {
            // since we need to check all neighbors, store off the result for reuse
            boolean value = false;
            for (ConnectedRoom neighbor : room.getNeighbors()) {
                if (!neighbor.getFaction().isPresent()
                        || !owningFaction.get().getId().equals(neighbor.getFaction().get())) {
                    value = true;
                    break;
                }
            }
            frontier.put(room, value);
        }
        return frontier.get(room);
    }
}
