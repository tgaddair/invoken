package com.eldritch.invoken.actor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eldritch.invoken.proto.Actors.DialogueTree;
import com.eldritch.invoken.proto.Actors.DialogueTree.Choice;
import com.eldritch.invoken.proto.Actors.DialogueTree.Response;

public class ConversationHandler {
    private final Map<String, Response> responses = new HashMap<String, Response>();
    private final Map<String, Choice> choices = new HashMap<String, Choice>();
    
	private final DialogueTree tree;
	private final DialogueVerifier verifier;
	private final boolean canSpeak;
	
	public ConversationHandler(DialogueTree tree, DialogueVerifier verifier) {
		this.tree = tree;
		this.verifier = verifier;
		canSpeak = !tree.getDialogueList().isEmpty();
		
		for (Response response : tree.getDialogueList()) {
		    responses.put(response.getId(), response);
		}
		for (Choice choice : tree.getChoiceList()) {
		    choices.put(choice.getId(), choice);
		}
	}
	
	public boolean canSpeak() {
	    return canSpeak;
	}
	
	public List<Choice> getChoicesFor(Response response) {
        List<Choice> validChoices = new ArrayList<Choice>();
        for (String id : response.getChoiceIdList()) {
            Choice choice = choices.get(id);
            if (verifier.isValid(choice)) {
                validChoices.add(choice);
            }
        }
        return validChoices;
    }
    
    public Response getResponseFor(Choice choice) {
        for (String id : choice.getSuccessorIdList()) {
            Response response = responses.get(id);
            if (verifier.isValid(response)) {
                return response;
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
