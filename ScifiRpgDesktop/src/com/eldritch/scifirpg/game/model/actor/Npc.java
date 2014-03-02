package com.eldritch.scifirpg.game.model.actor;

import java.util.List;

import com.eldritch.scifirpg.proto.Actors.DialogueTree;
import com.eldritch.scifirpg.proto.Actors.NonPlayerActor;
import com.eldritch.scifirpg.proto.Actors.DialogueTree.Response;
import com.eldritch.scifirpg.proto.Actors.NonPlayerActor.Aggression;
import com.eldritch.scifirpg.proto.Actors.NonPlayerActor.Assistance;
import com.eldritch.scifirpg.proto.Actors.NonPlayerActor.Confidence;
import com.eldritch.scifirpg.proto.Actors.NonPlayerActor.Trait;
import com.eldritch.scifirpg.proto.Locations.Encounter.ActorParams.ActorScenario;

public class Npc extends Actor {
    private final ActorModel actorModel;
    private final NonPlayerActor data;
    private final ActorScenario scenario;

    public Npc(NonPlayerActor data, ActorModel actorModel, ActorScenario scenario) {
        super(data.getParams());
        this.actorModel = actorModel;
        this.data = data;
        this.scenario = scenario;
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
            if (r.getGreeting() && this.actorModel.dialogueVerifier.isValid(r)) {
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
}