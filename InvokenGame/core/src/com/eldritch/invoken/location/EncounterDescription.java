package com.eldritch.invoken.location;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector2;
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
    }
}
