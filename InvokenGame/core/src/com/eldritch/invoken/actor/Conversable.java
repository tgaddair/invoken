package com.eldritch.invoken.actor;

import com.badlogic.gdx.math.Vector2;

public interface Conversable {
	ConversationHandler getDialogueHandler();
	
	boolean canConverse();
	
	void endDialogue();
	
	Vector2 getRenderPosition();
	
	float getWidth();
	
	float getHeight();
}
