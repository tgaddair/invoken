package com.eldritch.invoken.actor.type;

import com.badlogic.gdx.math.Vector2;

public class CoverPoint extends BasicSteerable {
    public CoverPoint(Vector2 position) {
        super(position);
    }
    
    @Override
    public float getBoundingRadius() {
        return 0.5f;
    }
}