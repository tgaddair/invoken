package com.eldritch.invoken.actor;

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
}
