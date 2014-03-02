package com.eldritch.scifirpg.game.model.actor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.eldritch.scifirpg.game.util.ActorMarshaller;
import com.eldritch.scifirpg.proto.Actors.DialogueTree.Response;
import com.eldritch.scifirpg.proto.Actors.NonPlayerActor;
import com.eldritch.scifirpg.proto.Locations.Encounter.ActorParams.ActorScenario;

public class ActorModel {
    private final Player player;
    private final ActorMarshaller actorMarshaller = new ActorMarshaller();
    private final Set<String> deadNpcs = new HashSet<>();
    final DialogueVerifier dialogueVerifier = new DialogueVerifier();
    
    public ActorModel(Player player) {
        this.player = player;
    }
    
    public Player getPlayer() {
        return player;
    }

    public List<Npc> getActorsFor(ActorEncounter encounter) {
        List<Npc> actors = new ArrayList<>();
        for (ActorScenario scenario : encounter.getScenarios()) {
            String id = scenario.getActorId();
            if (isAlive(id)) {
                Npc actor = new Npc(getActor(id), this, scenario);
                actors.add(actor);
            }
        }
        return actors;
    }

    public NonPlayerActor getActor(String id) {
        return actorMarshaller.readAsset(id);
    }
    
    public boolean isAlive(String id) {
        return !deadNpcs.contains(id);
    }

    public static class ParsedResponse {
        private final Response response;
        private final String parsedText;
        
        public ParsedResponse(Response response, String parsedText) {
            this.response = response;
            this.parsedText = parsedText;
        }

        public Response getResponse() {
            return response;
        }

        public String getParsedText() {
            return parsedText;
        }
    }
    
    public class DialogueVerifier {
        public boolean isValid(Response r) {
            // TODO use prerequisites and the current actor model state
            return true;
        }
    }
}
