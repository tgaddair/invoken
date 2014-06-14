package com.eldritch.invoken.actor.ai;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Npc;
import com.eldritch.invoken.encounter.Location;

public abstract class MovementRoutine implements Routine {
    protected final Npc npc;
    protected final Location location;
    
    /** how long we move in a single direction before turning */
    private final Vector2 velocity = new Vector2();
    
    public MovementRoutine(Npc npc, Location location) {
        this.npc = npc;
        this.location = location;
    }
    
    protected abstract void doMove(Vector2 velocityDelta, Location screen);
    
    protected void move(float delta, Location screen) {
        Vector2 velocityDelta = new Vector2(0, 0);
        doMove(velocityDelta, screen);

        // check that the tile immediately adjacent in the chosen direction is
        // not an obstacle
        avoidCollisions(velocityDelta, screen);

        // scale down the previous velocity to reduce the effects of momentum as
        // we're turning
        velocity.scl(0.75f);

        // add our velocity delta and clamp it to the max velocity
        velocity.add(velocityDelta);
        bound(velocity, npc.getMaxVelocity());

        resetVelocity();
    }

    protected void pursueTarget(Vector2 destination, Vector2 velocity, Location screen) {
        adjustVelocity(npc.getPosition(), destination, velocity, screen);
    }

    protected void fleeTarget(Vector2 destination, Vector2 velocity, Location screen) {
        adjustVelocity(destination, npc.getPosition(), velocity, screen);
    }
    
    private void adjustVelocity(Vector2 position, Vector2 target,
            Vector2 velocity, Location screen) {
        float dx = target.x - position.x;
        float dy = target.y - position.y;

        if (Math.abs(Math.abs(dx) - Math.abs(dy)) < 0.1) {
            // prevents flickering when moving along the diagonal
            velocity.x += dx;
            velocity.y += dy;
        } else if (Math.abs(dx) > Math.abs(dy)) {
            velocity.x += dx;
        } else {
            velocity.y += dy;
        }
    }

    private void avoidCollisions(Vector2 velocity, Location screen) {
        Vector2 position = npc.getPosition().cpy();
        int i1 = (int) position.x - 1;
        int i2 = (int) position.x + 1;
        int j1 = (int) position.y - 1;
        int j2 = (int) position.y + 1;

        Vector2 obstacleMass = new Vector2(0, 0);
        int totalObstacles = 0;
        for (int i = i1; i <= i2; i++) {
            for (int j = j1; j <= j2; j++) {
                if (screen.isObstacle(i, j)) {
                    obstacleMass.add(i, j);
                    totalObstacles++;
                }
            }
        }

        if (totalObstacles > 0) {
            obstacleMass.scl(1f / totalObstacles);
            position.sub(obstacleMass).scl(1f);
            velocity.add(position);
        }
    }

    private void resetVelocity() {
        npc.setVelocity(velocity.x, velocity.y);
    }
    
    private void bound(Vector2 vector, float tol) {
        vector.x = Math.min(Math.max(vector.x, -tol), tol);
        vector.y = Math.min(Math.max(vector.y, -tol), tol);
    }
}
