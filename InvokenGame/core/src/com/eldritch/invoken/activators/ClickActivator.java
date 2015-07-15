package com.eldritch.invoken.activators;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.location.NaturalVector2;

public abstract class ClickActivator extends ProximityActivator {
    private final int width;
    private final int height;
    private final Vector2 center;

    public ClickActivator(NaturalVector2 position) {
        this(position, 1, 1);
    }

    public ClickActivator(NaturalVector2 position, int width, int height) {
        this(position.x, position.y, width, height);
    }

    public ClickActivator(float x, float y, int width, int height) {
        this(x, y, width, height, new Vector2(x + width / 2, y + height / 2));
    }
    
    public ClickActivator(float x, float y, int width, int height, Vector2 center) {
        super(x, y, center, Vector2.Zero);
        this.width = width;
        this.height = height;
        this.center = center;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    @Override
    public boolean click(Agent agent, Level level, float x, float y) {
        Vector2 position = getCenter();
        boolean clicked = x >= position.x - width / 2f && x <= position.x + width / 2f
                && y >= position.y - height / 2f && y <= position.y + height / 2f;
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
        return hasProximity(agent);
    }
    
    @Override
    protected boolean onProximityChange(boolean hasProximity, Level level) {
        return true;
    }

    protected Vector2 getCenter() {
        return center;
    }
}
