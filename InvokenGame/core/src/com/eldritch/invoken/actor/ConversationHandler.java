package com.eldritch.invoken.actor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.eldritch.invoken.proto.Actors.DialogueTree;
import com.eldritch.invoken.proto.Actors.DialogueTree.Choice;
import com.eldritch.invoken.proto.Actors.DialogueTree.Response;
import com.google.common.collect.Maps;

public class ConversationHandler {
    private final Map<String, Response> responses = Maps.newHashMap();
    private final Map<String, Choice> choices = Maps.newHashMap();
    private final Map<DialogueTree, List<Response>> greetings = Maps.newHashMap();
    
	private final List<DialogueTree> trees;
	private final DialogueVerifier verifier;
	private final boolean canSpeak;
	
	public ConversationHandler(List<DialogueTree> trees, DialogueVerifier verifier) {
		this.trees = trees;
		this.verifier = verifier;
		canSpeak = !trees.isEmpty();
		
		for (DialogueTree tree : trees) {
		    greetings.put(tree, new ArrayList<Response>());
    		for (Response response : tree.getDialogueList()) {
    		    responses.put(response.getId(), response);
    		    if (response.getGreeting()) {
    		        greetings.get(tree).add(response);
    		    }
    		}
    		for (Choice choice : tree.getChoiceList()) {
    		    choices.put(choice.getId(), choice);
    		}
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
        return !greetings.isEmpty();
    }
    
    public Response getGreeting() {
//        if (scenario.hasDialogue()) {
//            Response greeting = getGreetingFor(scenario.getDialogue());
//            if (greeting != null) {
//                return greeting;
//            }
//        }
        
        for (DialogueTree tree : trees) {
            return getGreetingFor(tree);
        }
        return null;
    }
    
    private Response getGreetingFor(DialogueTree tree) {
        if (greetings.containsKey(tree)) {
            for (Response r : greetings.get(tree)) {
                if (r.getGreeting() && verifier.isValid(r)) {
                    return r;
                }
            }
        }
        return null;
    }
    
    public static interface DialogueVerifier {
    	boolean isValid(Response r);
    	
    	boolean isValid(Choice c);
    }
}
