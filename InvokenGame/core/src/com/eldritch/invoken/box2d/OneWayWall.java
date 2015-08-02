package com.eldritch.invoken.box2d;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.box2d.AgentHandler.DefaultAgentHandler;

public interface OneWayWall {
    boolean hasContact(Vector2 direction);

    public static class OneWayWallImpl extends DefaultAgentHandler implements OneWayWall {
        private static final double TOLERANCE = Math.PI / 2;

        private final Vector2 normal = new Vector2();

        public OneWayWallImpl(Vector2 normal) {
            this.normal.set(normal);
        }

        @Override
        public boolean hasContact(Vector2 direction) {
            float theta = normal.angleRad(direction);
            return Math.abs(theta) < TOLERANCE;
        }
    }
}
