package com.eldritch.invoken.location;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.eldritch.invoken.actor.factions.Faction;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.util.Constants;

/**
 * The crime manager is a publish / subscribe system that relays crimes committed by a perpetrator
 * to a set of crime handlers that listen for crimes committed against certain individuals (e.g.,
 * specific people or allied factions).
 */
public class CrimeManager {
    private static float ASSAULT_SEVERITY = 10f;
    private static float VANDALISM_SEVERITY = 5f;
    private static float THEFT_SEVERITY = 1f;

    private final Faction stationFaction;
    private final List<CrimeHandler> handlers = new ArrayList<>();
    private final ConnectedRoomManager rooms;

    public CrimeManager(Level level, ConnectedRoomManager rooms) {
        this.stationFaction = level.getFaction(Constants.STATION_FACTION);
        this.rooms = rooms;
    }

    public void commitAssault(Agent perp, Agent victim) {
        notify(new AgentCrime(perp, victim, ASSAULT_SEVERITY));
    }

    public void commitVandalism(Agent perp, NaturalVector2 point) {
        notify(new FactionCrime(perp, stationFaction, point, VANDALISM_SEVERITY));
    }

    public void commitTheft() {

    }

    public void addHandler(CrimeHandler handler) {
        handlers.add(handler);
    }

    private void notify(Crime crime) {
        for (CrimeHandler handler : handlers) {
            handler.handle(crime);
        }
    }

    public abstract class Crime {
        // we need a handler to report the crime, but to avoid double counting, we keep track of the
        // state here
        private final Set<Faction> reported = new HashSet<>();
        private final Agent perpetrator;
        private final NaturalVector2 point;
        private final float severity;

        private Crime(Agent perpetrator, NaturalVector2 point, float severity) {
            this.perpetrator = perpetrator;
            this.point = point;
            this.severity = severity;
        }

        public Agent getPerpetrator() {
            return perpetrator;
        }

        public float getSeverity() {
            return severity;
        }

        public void report(Faction faction) {
            if (!reported.contains(faction)) {
                perpetrator.changeFactionStatus(faction, -severity);
                reported.add(faction);
            }
        }

        public NaturalVector2 getPoint() {
            return point;
        }

        public boolean hasRoom() {
            return rooms.hasRoom(point.x, point.y);
        }

        public ConnectedRoom getRoom() {
            return rooms.getRoom(point.x, point.y);
        }

        // was the crime committed an offsense against the given faction
        public abstract boolean isOffenseAgainst(Faction faction);
    }

    public class AgentCrime extends Crime {
        private final Agent victim;

        private AgentCrime(Agent perpetrator, Agent victim, float severity) {
            super(perpetrator, victim.getCellPosition(), severity);
            this.victim = victim;
        }

        public Agent getVictim() {
            return victim;
        }

        @Override
        public boolean isOffenseAgainst(Faction faction) {
            return victim.isAllyOf(faction);
        }
    }

    public class FactionCrime extends Crime {
        private final Faction offended;

        private FactionCrime(Agent perpetrator, Faction offended, NaturalVector2 point,
                float severity) {
            super(perpetrator, point, severity);
            this.offended = offended;
        }

        public Faction getOffended() {
            return offended;
        }

        @Override
        public boolean isOffenseAgainst(Faction faction) {
            return faction.getRelation(offended) > 0;
        }
    }

    public interface CrimeHandler {
        void handle(Crime crime);
    }
}
