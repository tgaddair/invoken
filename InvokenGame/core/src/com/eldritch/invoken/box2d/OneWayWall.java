package com.eldritch.invoken.box2d;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.box2d.AgentHandler.DefaultAgentHandler;

public class OneWayWall extends DefaultAgentHandler {
    private static final double TOLERANCE = Math.PI / 2;
    
    private final Vector2 normal = new Vector2();
    
    public OneWayWall(Vector2 normal) {
        this.normal.set(normal);
    }
    
    public boolean hasContact(Vector2 force) {
        float theta = normal.angleRad(force);
        return Math.abs(theta) < TOLERANCE;
    }
}
