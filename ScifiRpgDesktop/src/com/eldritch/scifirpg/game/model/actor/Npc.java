package com.eldritch.scifirpg.game.model.actor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.eldritch.scifirpg.game.model.ActionAugmentation;
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
    private final Set<Actor> enemies = new HashSet<>();

    public Npc(NonPlayerActor data, ActorModel actorModel, ActorScenario scenario) {
        super(data.getParams());
        this.model = actorModel;
        this.data = data;
        this.scenario = scenario;
        
        // Construct augs and items by randomly sampling from available
        for (String augId : getKnownAugmentations()) {
            Augmentation aug = AUG_READER.readAsset(augId);
            stage(new ActionAugmentation(aug, this, 20));
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
    public void takeCombatTurn(ActorEncounterModel model) {
        Actor target = null;
        for (Actor actor : enemies) {
            // TODO pick the enemy that poses the biggest threat, or we hate the most, etc.
            target = actor;
        }
        
        ActionAugmentation action = null;
        for (ActionAugmentation aug : getActions()) {
            if (aug.getType() == Type.ATTACK) {
                action = aug;
            }
        }
        
        if (action != null && target != null) {
            // Attack the target
            model.invoke(action, target);
        } else {
            // Pass
            model.passCombat();
        }
    }
    
    @Override
    public boolean handleAttack(ActionAugmentation attack, Collection<Actor> combatants) {
        enemies.add(attack.getOwner());
        return super.handleAttack(attack, combatants);
    }
    
    @Override
    public boolean hasEnemy() {
        boolean found = false;
        Iterator<Actor> it = enemies.iterator();
        while (it.hasNext()) {
            // TODO recalculate aggression maybe?
            Actor actor = it.next();
            if (actor.isAlive()) {
                found = true;
            } else {
                it.remove();
            }
        }
        return found;
    }
    
    public boolean hasEnemy(Actor actor) {
        return enemies.contains(actor);
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