package com.eldritch.invoken.actor.type;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.eldritch.invoken.actor.Entity;
import com.eldritch.invoken.encounter.Location;

public abstract class CollisionEntity implements Entity {
    protected final Vector2 position = new Vector2();
    protected final Vector2 velocity = new Vector2();
    private final float width;
    private final float height;
    
    public CollisionEntity(float width, float height) {
        this.width = width;
        this.height = height;
    }
    
    protected Array<Agent> getCollisionActors(Location screen) {
        Array<Agent> agents = new Array<Agent>();
        for (Agent other : screen.getActors()) {
            // only collide with enemies
            if (other == this)
                continue;

            // avoid sqrt because it is relatively expensive and unnecessary
            float a = position.x - other.position.x;
            float b = position.y - other.position.y;
            float distance = a * a + b * b;

            // our tolerance is the combined radii of both actors
            float w = width / 2 + other.getWidth() / 2;
            float h = height / 2 + other.getHeight() / 2;
            float tol = w * w + h * h;

            if (distance <= tol) {
                agents.add(other);
            }
        }
        return agents;
    }
    
    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }
}
