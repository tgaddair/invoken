package com.eldritch.invoken.activators;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Player;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.location.NaturalVector2;

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
	
	public ClickActivator(float x, float y, int width, int height) {
        super(x, y);
        this.width = width;
        this.height = height;
    }
	
	public float getWidth() {
	    return width;
	}
	
	public float getHeight() {
	    return height;
	}
	
	@Override
	public void update(float delta, Level level) {
	    // does nothing
	}
	
	@Override
	public boolean click(Agent agent, Level level, float x, float y) {
	    Vector2 position = getPosition();
		boolean clicked = x >= position.x && x <= position.x + width && y >= position.y
                && y <= position.y + height;
        if (clicked && canActivate(agent)) {
            // first, attempt to handle the click event with a handler
            if (!agent.handle(this)) {
                // could not handle, so delegate to the activation mechanism
                activate(agent, level);
            }
        }
        return clicked;
	}
	
	protected boolean canActivate(Agent agent) {
	    return agent.getPosition().dst2(getCenter()) < 6;
	}
	
	protected Vector2 getCenter() {
	    return getPosition();
	}
}
