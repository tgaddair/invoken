package com.eldritch.invoken.actor;

public class DialogueManager {
	Agent dialoguer = null;
	
	public void setDialogue(Agent other) {
		dialoguer = other;
	}
	
	public Agent getDialoguer() {
		return dialoguer;
	}
	
	public boolean inDialogue() {
		return dialoguer != null;
	}
}
