package com.eldritch.invoken.actor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.eldritch.invoken.proto.Actors.DialogueTree;
import com.eldritch.invoken.proto.Actors.DialogueTree.Choice;
import com.eldritch.invoken.proto.Actors.DialogueTree.Response;

public class ConversationHandler {
	private final DialogueTree tree;
	private final DialogueVerifier verifier;
	
	public ConversationHandler(DialogueTree tree, DialogueVerifier verifier) {
		this.tree = tree;
		this.verifier = verifier;
	}
	
	public List<Choice> getChoicesFor(Response response) {
        List<Choice> choices = new ArrayList<Choice>();
        for (Choice choice : response.getChoiceList()) {
            if (verifier.isValid(choice)) {
                choices.add(choice);
            }
        }
        return choices;
    }
    
    public Response getResponseFor(Choice choice) {
        Set<String> successors = new HashSet<String>(choice.getSuccessorIdList());
        for (Response r : tree.getDialogueList()) {
            if (successors.contains(r.getId()) && verifier.isValid(r)) {
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
//        if (scenario.hasDialogue()) {
//            Response greeting = getGreetingFor(scenario.getDialogue());
//            if (greeting != null) {
//                return greeting;
//            }
//        }
        return getGreetingFor(tree);
    }
    
    private Response getGreetingFor(DialogueTree tree) {
        for (Response r : tree.getDialogueList()) {
            if (r.getGreeting() && verifier.isValid(r)) {
                return r;
            }
        }
        return null;
    }
    
    public static interface DialogueVerifier {
    	boolean isValid(Response r);
    	
    	boolean isValid(Choice c);
    }
}
