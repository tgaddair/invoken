package com.eldritch.invoken.activators;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.encounter.NaturalVector2;

public abstract class ClickActivator extends BasicActivator {
	private final int width;
	private final int height;
	
	public ClickActivator(NaturalVector2 position) {
	    this(position, 1, 1);
	}
	
	public ClickActivator(NaturalVector2 position, int width, int height) {
	    super(position);
	    this.width = width;
	    this.height = height;
	}
	
	public int getWidth() {
	    return width;
	}
	
	public int getHeight() {
	    return height;
	}
	
	@Override
	public void update(float delta, Location location) {
	    // does nothing
	}
	
	@Override
	public boolean click(Agent agent, Location location, float x, float y) {
	    Vector2 position = getPosition();
		boolean clicked = x >= position.x && x <= position.x + width && y >= position.y
                && y <= position.y + height;
        if (clicked && canActivate(agent, x, y)) {
            // first, attempt to handle the click event with a handler
            if (!agent.handle(this)) {
                // could not handle, so delegate to the activation mechanism
                activate(agent, location);
            }
        }
        return clicked;
	}
	
	protected boolean canActivate(Agent agent, float x, float y) {
	    return agent.getPosition().dst2(getPosition()) < 6;
	}
}
