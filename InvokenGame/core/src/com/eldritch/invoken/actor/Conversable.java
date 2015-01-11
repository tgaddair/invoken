package com.eldritch.invoken.actor;

import java.util.List;

import com.eldritch.invoken.proto.Actors.DialogueTree;
import com.eldritch.invoken.proto.Actors.DialogueTree.Choice;
import com.eldritch.invoken.proto.Actors.DialogueTree.Response;

public interface Conversable {
	List<Choice> getChoicesFor(Response response);
	
	Response getResponseFor(Choice choice);
	
	boolean hasGreeting();
	
	Response getGreeting();
}
