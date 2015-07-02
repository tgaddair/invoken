package com.eldritch.invoken.actor.util;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.eldritch.invoken.location.NaturalVector2;

public interface Locatable {
    Vector2 getPosition();
    
    NaturalVector2 getNaturalPosition();
    
    Vector2 getPhysicalPosition();
    
    Iterable<Fixture> getFixtures();
}
