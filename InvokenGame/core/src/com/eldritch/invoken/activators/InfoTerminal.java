package com.eldritch.invoken.activators;

import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.eldritch.invoken.actor.Conversable;
import com.eldritch.invoken.actor.ConversationHandler;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.location.Location;
import com.eldritch.invoken.location.NaturalVector2;

public class InfoTerminal extends ClickActivator implements Conversable {
	private final ConversationHandler dialogue;
	
	public InfoTerminal(NaturalVector2 position) {
		super(position);
//		dialogue = new ConversationHandler();
		dialogue = null;
	}

	@Override
	public void activate(Agent agent, Location location) {
	}

	@Override
	public void register(Location location) {
	}

	@Override
	public ConversationHandler getDialogueHandler() {
		return dialogue;
	}

    @Override
    public void render(float delta, OrthogonalTiledMapRenderer renderer) {
        // delegated to layer
    }
}
