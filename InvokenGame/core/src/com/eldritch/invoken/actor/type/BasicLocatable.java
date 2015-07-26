package com.eldritch.invoken.actor.type;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.eldritch.invoken.actor.util.Locatable;
import com.eldritch.invoken.location.NaturalVector2;
import com.google.common.collect.ImmutableList;

public class BasicLocatable implements Locatable {
    protected final Vector2 position = new Vector2();
    
    public BasicLocatable(NaturalVector2 point) {
        this(point.toVector2());
    }
    
    public BasicLocatable(Vector2 point) {
        this.position.set(point);
    }
    
    public void setPosition(Vector2 point) {
        this.position.set(point);
    }
    
    @Override
    public Vector2 getPosition() {
        return position;
    }
    
    @Override
    public NaturalVector2 getNaturalPosition() {
        return NaturalVector2.of((int) getPosition().x, (int) getPosition().y);
    }

    @Override
    public Vector2 getPhysicalPosition() {
        return getPosition();
    }

    @Override
    public Iterable<Fixture> getFixtures() {
        return ImmutableList.of();
    }
}
