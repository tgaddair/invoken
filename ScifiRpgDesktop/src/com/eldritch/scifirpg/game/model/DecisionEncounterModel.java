package com.eldritch.scifirpg.game.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.eldritch.scifirpg.game.model.actor.ActorModel;
import com.eldritch.scifirpg.game.util.PrerequisiteVerifier;
import com.eldritch.invoken.proto.Actors.DialogueTree;
import com.eldritch.invoken.proto.Actors.DialogueTree.Choice;
import com.eldritch.invoken.proto.Actors.DialogueTree.Response;

public class DecisionEncounterModel extends EncounterModel<DecisionEncounter, EncounterListener> {
    private final DecisionVerifier verifier;
    
    public DecisionEncounterModel(DecisionEncounter encounter, GameState state) {
        super(encounter, state);
        verifier = new DecisionVerifier(state.getActorModel());
    }
    
    public void init() {
        // Once we have our listeners in place, go ahead and apply outcomes
        //applyOutcomes(getEncounter().getOutcomes());
    }
    
    public boolean hasGreeting() {
        // TODO this could be more efficient
        return getGreeting() != null;
    }
    
    public Response getGreeting() {
        return getGreetingFor(getEncounter().getDecisionTree());
    }
    
    private Response getGreetingFor(DialogueTree tree) {
        for (Response r : tree.getDialogueList()) {
            if (r.getGreeting() && verifier.isValid(r)) {
                return r;
            }
        }
        return null;
    }
    
    public List<Choice> getChoicesFor(Response response) {
        // Apply outcomes for choice
        applyOutcomes(response.getOutcomeList());
        
        List<Choice> choices = new ArrayList<>();
        for (Choice choice : response.getChoiceList()) {
            if (verifier.isValid(choice)) {
                choices.add(choice);
            }
        }
        return choices;
    }
    
    public Response getResponseFor(Choice choice) {
        Set<String> successors = new HashSet<>(choice.getSuccessorIdList());
        for (Response r : getEncounter().getDecisionTree().getDialogueList()) {
            if (successors.contains(r.getId()) && verifier.isValid(r)) {
                return r;
            }
        }
        return null;
    }
    
    private class DecisionVerifier extends PrerequisiteVerifier {
        private final ActorModel actorModel;
        
        public DecisionVerifier(ActorModel actorModel) {
            this.actorModel = actorModel;
        }
        
        public boolean isValid(Response r) {
            return verify(r.getPrereqList(), actorModel);
        }
        
        public boolean isValid(Choice c) {
            return verify(c.getPrereqList(), actorModel);
        }
    }
}
