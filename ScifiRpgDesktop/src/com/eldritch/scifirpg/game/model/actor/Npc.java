package com.eldritch.scifirpg.game.model.actor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.eldritch.scifirpg.game.model.ActionAugmentation;
import com.eldritch.scifirpg.proto.Actors.DialogueTree;
import com.eldritch.scifirpg.proto.Actors.DialogueTree.Choice;
import com.eldritch.scifirpg.proto.Actors.NonPlayerActor;
import com.eldritch.scifirpg.proto.Actors.DialogueTree.Response;
import com.eldritch.scifirpg.proto.Actors.NonPlayerActor.Aggression;
import com.eldritch.scifirpg.proto.Actors.NonPlayerActor.Assistance;
import com.eldritch.scifirpg.proto.Actors.NonPlayerActor.Confidence;
import com.eldritch.scifirpg.proto.Actors.NonPlayerActor.Trait;
import com.eldritch.scifirpg.proto.Locations.Encounter.ActorParams.ActorScenario;

public class Npc extends Actor {
    private final DialogueVerifier dialogueVerifier = new DialogueVerifier();
    private final NonPlayerActor data;
    private final ActorScenario scenario;
    private final Set<Actor> enemies = new HashSet<>();

    public Npc(NonPlayerActor data, ActorModel actorModel, ActorScenario scenario) {
        super(data.getParams());
        this.data = data;
        this.scenario = scenario;
        // TODO construct enemies from the encounter
    }
    
    public boolean handleAttack(ActionAugmentation attack) {
        enemies.add(attack.getOwner());
        return true;
    }
    
    public Response getResponseFor(Choice choice) {
        Set<String> successors = new HashSet<>(choice.getSuccessorIdList());
        for (Response r : data.getDialogue().getDialogueList()) {
            if (successors.contains(r.getId()) && dialogueVerifier.isValid(r)) {
                return r;
            }
        }
        return null;
    }
    
    public Response getGreeting() {
        if (scenario.hasDialogue()) {
            Response greeting = getGreetingFor(scenario.getDialogue());
            if (greeting != null) {
                return greeting;
            }
        }
        return getGreetingFor(data.getDialogue());
    }
    
    private Response getGreetingFor(DialogueTree tree) {
        for (Response r : tree.getDialogueList()) {
            if (r.getGreeting() && dialogueVerifier.isValid(r)) {
                return r;
            }
        }
        return null;
    }

    public boolean isUnique() {
        return data.getUnique();
    }

    public boolean canSpeak() {
        return data.getCanSpeak();
    }

    public DialogueTree getDialogueTree() {
        return data.getDialogue();
    }

    public Aggression getBaseAggression() {
        return data.getAggression();
    }

    public Assistance getBaseAssistance() {
        return data.getAssistance();
    }

    public Confidence getBaseConfidence() {
        return data.getConfidence();
    }

    public List<Trait> getTraits() {
        return data.getTraitList();
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