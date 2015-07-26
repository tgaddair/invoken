package com.eldritch.invoken.location;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.type.Npc;
import com.eldritch.invoken.proto.Actors.NonPlayerActor;
import com.eldritch.invoken.proto.Locations.Encounter.ActorParams.ActorScenario;
import com.google.common.base.Optional;

public class EncounterDescription {
    private final List<AgentDescription> agents = new ArrayList<>();

    public void addAgent(AgentDescription agent) {
        agents.add(agent);
    }

    public List<AgentDescription> getAgents() {
        return agents;
    }

    public static class AgentDescription {
        private final ActorScenario scenario;
        private final Vector2 position;
        private final Optional<ConnectedRoom> room;

        public AgentDescription(ActorScenario scenario, NaturalVector2 position, ConnectedRoom room) {
            this.scenario = scenario;
            this.position = position.toVector2();
            this.room = Optional.fromNullable(room);
        }

        public Vector2 getPosition() {
            return position;
        }

        public ActorScenario getScenario() {
            return scenario;
        }

        public boolean hasRoom() {
            return room.isPresent();
        }

        public ConnectedRoom getRoom() {
            return room.get();
        }

        public Npc create(Level level) {
            float x = position.x + 0.5f;
            float y = position.y + 0.5f;
            NonPlayerActor proto = InvokenGame.ACTOR_READER.readAsset(scenario.getActorId());
            
            Npc npc = Npc.create(proto, x, y, level);
            if (hasRoom()) {
                // give the NPC the key
                ConnectedRoom room = getRoom();
                npc.getInventory().addItem(room.getKey());
                room.addResident(npc);
            }
            
            return npc;
        }
    }
}
