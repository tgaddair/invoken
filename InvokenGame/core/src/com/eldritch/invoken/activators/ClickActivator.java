package com.eldritch.invoken.activators;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.encounter.Location;

public abstract class ClickActivator implements Activator {
	private final Vector2 position = new Vector2();
	
	public ClickActivator(int x, int y) {
		position.set(x, y);
	}
	
	@Override
	public boolean click(Agent agent, Location location, float x, float y) {
		boolean clicked = x >= position.x && x <= position.x + 1 && y >= position.y
                && y <= position.y + 1;
        if (clicked) {
            activate(agent, location);
        }
        return clicked;
	}
}
