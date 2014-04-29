package com.eldritch.invoken.actor;

import com.eldritch.scifirpg.proto.Actors.DialogueTree.Response;

public class DialogueManager {
	Npc dialoguer = null;
	
	public void setDialogue(Npc other) {
		dialoguer = other;
	}
	
	public Npc getDialoguer() {
		return dialoguer;
	}
	
	public boolean inDialogue() {
		return dialoguer != null;
	}
	
	public Response getGreeting() {
		return dialoguer.getGreeting();
	}
}
