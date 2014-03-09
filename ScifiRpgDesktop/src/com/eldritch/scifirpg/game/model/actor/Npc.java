package com.eldritch.scifirpg.game.model.actor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.eldritch.scifirpg.game.model.ActiveAugmentation;
import com.eldritch.scifirpg.game.util.PrerequisiteVerifier;
import com.eldritch.scifirpg.proto.Actors.DialogueTree;
import com.eldritch.scifirpg.proto.Actors.DialogueTree.Choice;
import com.eldritch.scifirpg.proto.Actors.NonPlayerActor;
import com.eldritch.scifirpg.proto.Actors.DialogueTree.Response;
import com.eldritch.scifirpg.proto.Actors.NonPlayerActor.Aggression;
import com.eldritch.scifirpg.proto.Actors.NonPlayerActor.Assistance;
import com.eldritch.scifirpg.proto.Actors.NonPlayerActor.Confidence;
import com.eldritch.scifirpg.proto.Actors.NonPlayerActor.Trait;
import com.eldritch.scifirpg.proto.Augmentations.Augmentation;
import com.eldritch.scifirpg.proto.Augmentations.Augmentation.Type;
import com.eldritch.scifirpg.proto.Locations.Encounter.ActorParams.ActorScenario;
import com.eldritch.scifirpg.proto.Outcomes.Outcome;
import com.eldritch.scifirpg.proto.Prerequisites.Prerequisite;
import com.eldritch.scifirpg.proto.Prerequisites.Standing;

public class Npc extends Actor {
    private final DialogueVerifier dialogueVerifier = new DialogueVerifier();
    private final ActorModel model;
    private final NonPlayerActor data;
    private final ActorScenario scenario;

    public Npc(NonPlayerActor data, ActorModel actorModel, ActorScenario scenario) {
        super(data.getParams());
        this.model = actorModel;
        this.data = data;
        this.scenario = scenario;
        
        // Construct augs and items by randomly sampling from available
        for (String augId : getKnownAugmentations()) {
            Augmentation aug = AUG_READER.readAsset(augId);
            stage(new ActiveAugmentation(aug, this, 20));
        }
        
        // TODO construct enemies from the encounter
    }
    
    public int getInfluence(Actor other) {
        // TODO
        return 0;
    }
    
    public Standing getStanding(Actor other) {
        // TODO
        return Standing.NEUTRAL;
    }
    
    @Override
    public void takeCombatTurn(ActionModel model) {
        ActorState state = model.getState(this);
        ActorState target = null;
        for (ActorState actor : state.getEnemies()) {
            // TODO pick the enemy that poses the biggest threat, or we hate the most, etc.
            target = actor;
        }
        
        ActiveAugmentation chosenAug = null;
        for (ActiveAugmentation aug : getActions()) {
            if (aug.getType() == Type.ATTACK) {
                chosenAug = aug;
            }
        }
        
        if (chosenAug != null && target != null) {
            Action action = new Action(chosenAug, state, target);
            model.takeAction(action);
        } else {
            // Pass
            model.passCombat(state);
        }
    }
    
    public List<Choice> getChoicesFor(Response response) {
        List<Choice> choices = new ArrayList<>();
        for (Choice choice : response.getChoiceList()) {
            if (dialogueVerifier.isValid(choice)) {
                choices.add(choice);
            }
        }
        return choices;
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
    
    public boolean hasGreeting() {
        // TODO this could be more efficient
        return getGreeting() != null;
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
    
    public boolean isBlocking() {
        return scenario.getBlocking();
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
    
    public List<Outcome> getDeathOutcomes() {
        return scenario.getOnDeathList();
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
    
    public class DialogueVerifier extends PrerequisiteVerifier {
        @Override
        protected boolean verifyInfluence(Prerequisite prereq, Actor actor) {
            int value = getInfluence(actor);
            return verifyBetween(prereq, value);
        }
        
        @Override
        protected boolean verifyStanding(Prerequisite prereq, Actor actor) {
            Standing value = getStanding(actor);
            boolean has = value == Standing.valueOf(prereq.getTarget());
            return verifyHas(prereq, has);
        }
        
        public boolean isValid(Response r) {
            return verify(r.getPrereqList(), model);
        }
        
        public boolean isValid(Choice c) {
            return verify(c.getPrereqList(), model);
        }
    }
}