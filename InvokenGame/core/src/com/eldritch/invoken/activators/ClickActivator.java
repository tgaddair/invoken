package com.eldritch.invoken.activators;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.encounter.NaturalVector2;

public abstract class ClickActivator implements Activator {
	private final Vector2 position = new Vector2();
	
	public ClickActivator(NaturalVector2 position) {
		this.position.set(position.x, position.y);
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
	
	public Vector2 getPosition() {
		return position;
	}
}
