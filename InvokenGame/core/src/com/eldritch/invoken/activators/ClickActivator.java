package com.eldritch.invoken.activators;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.encounter.NaturalVector2;

public abstract class ClickActivator implements Activator {
	private final Vector2 position = new Vector2();
	private final int width;
	private final int height;
	
	public ClickActivator(NaturalVector2 position) {
	    this(position, 1, 1);
	}
	
	public ClickActivator(NaturalVector2 position, int width, int height) {
	    this.position.set(position.x, position.y);
	    this.width = width;
	    this.height = height;
	}
	
	@Override
	public boolean click(Agent agent, Location location, float x, float y) {
		boolean clicked = x >= position.x && x <= position.x + width && y >= position.y
                && y <= position.y + height;
        if (clicked) {
            activate(agent, location);
        }
        return clicked;
	}
	
	@Override
	public float getZ() {
	    return position.y;
	}
	
	public Vector2 getPosition() {
		return position;
	}
}
