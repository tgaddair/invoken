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
    private final Vector2 clickPosition = new Vector2();
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
        this(x, y, width, height, Vector2.Zero, params);
    }

    public ClickActivator(float x, float y, int width, int height, Vector2 clickOffset,
            ProximityParams params) {
        super(x, y, params);
        this.clickPosition.set(x, y).add(clickOffset);
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
        Vector2 position = clickPosition;
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
        sr.rect(clickPosition.x, clickPosition.y, width, height);
        sr.end();
    }
}
