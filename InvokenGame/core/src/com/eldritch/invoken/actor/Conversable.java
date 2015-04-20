package com.eldritch.invoken.actor;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.util.Interactable;

public interface Conversable extends Interactable {
	ConversationHandler getDialogueHandler();
	
	boolean canConverse();
	
	void endDialogue();
	
	Vector2 getRenderPosition();
	
	float getWidth();
	
	float getHeight();
}
