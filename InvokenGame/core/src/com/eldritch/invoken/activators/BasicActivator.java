package com.eldritch.invoken.activators;

import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.location.Location;
import com.eldritch.invoken.location.NaturalVector2;

public abstract class BasicActivator implements Activator {
    private final Vector2 position = new Vector2();
    
    public BasicActivator(NaturalVector2 position) {
        this(position.x, position.y);
    }
    
    public BasicActivator(float x, float y) {
        this.position.set(x, y);
    }
    
    @Override
    public boolean click(Agent agent, Location location, float x, float y) {
        return false;
    }
    
    @Override
    public void register(Location location) {
    }

    @Override
    public void render(float delta, OrthogonalTiledMapRenderer renderer) {
    }
    
    @Override
    public float getZ() {
        return position.y;
    }
    
    public Vector2 getPosition() {
        return position;
    }
}
