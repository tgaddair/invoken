package com.eldritch.invoken.actor.type;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.eldritch.invoken.actor.Entity;
import com.eldritch.invoken.actor.util.Locatable;
import com.eldritch.invoken.location.Location;
import com.eldritch.invoken.location.NaturalVector2;

public abstract class CollisionEntity implements Entity, Locatable {
    protected final Vector2 position = new Vector2();
    protected final Vector2 velocity = new Vector2();
    protected final Vector2 heading = new Vector2();
    private final float width;
    private final float height;
    
    public CollisionEntity(float width, float height) {
        this.width = width;
        this.height = height;
    }
    
    protected Array<Agent> getCollisionActors(Location screen) {
        Array<Agent> agents = new Array<Agent>();
        for (Agent other : screen.getActiveEntities()) {
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
    
    @Override
    public float getZ() {
        return getPosition().y;
    }
    
    public Vector2 getPosition() {
        return position;
    }
    
    public Vector2 getVelocity() {
        return velocity;
    }
    
    public NaturalVector2 getNaturalPosition() {
        return NaturalVector2.of((int) getPosition().x, (int) getPosition().y);
    }
    
    protected void updateHeading() {
        if (velocity.len2() > 1E-6) {
            heading.set(velocity).nor();
        }
    }
    
    public Vector2 getHeading() {
        return heading.cpy();
    }
    
    public Vector2 getReverseHeading() {
        return getHeading().scl(-1);
    }
    
    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }
}
