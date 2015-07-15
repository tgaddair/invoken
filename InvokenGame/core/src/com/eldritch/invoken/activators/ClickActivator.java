package com.eldritch.invoken.activators;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.location.NaturalVector2;

public abstract class ClickActivator extends ProximityActivator {
    private final ShapeRenderer sr = new ShapeRenderer();
    private final int width;
    private final int height;

    public ClickActivator(NaturalVector2 position) {
        this(position, 1, 1);
    }

    public ClickActivator(NaturalVector2 position, int width, int height) {
        this(position.x, position.y, width, height);
    }

    public ClickActivator(float x, float y, int width, int height) {
        this(x, y, width, height, ProximityParams.of(x, y, width, height));
    }
    
    public ClickActivator(float x, float y, int width, int height, ProximityParams params) {
        super(x, y, params);
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
    
    public void renderClickArea(OrthographicCamera camera) {
        sr.setProjectionMatrix(camera.combined);
        sr.begin(ShapeType.Line);
        sr.setColor(Color.CYAN);
        Vector2 position = getCenter();
        sr.rect(position.x - width / 2f, position.y - height / 2f, width, height);
        sr.end();
    }
}
