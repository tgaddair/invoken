package com.eldritch.invoken.actor.ai;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.eldritch.invoken.actor.type.Npc;
import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.encounter.NaturalVector2;

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
    
    protected void move(float delta, Location screen, boolean avoid) {
        Vector2 velocityDelta = new Vector2(0, 0);
        doMove(velocityDelta, screen);

        // scale down the previous velocity to reduce the effects of momentum as
        // we're turning
        velocity.scl(0.75f);
        
        if (npc.getTarget() != null) {
            Vector2 steeringForce = avoidWalls(npc.getPosition(), npc.getTarget().getPosition(), screen);
            if (steeringForce != null) {
//                System.out.println("steering: " + steeringForce);
                velocityDelta.add(steeringForce);
            }
        }

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
    
    private Vector2 avoidWalls(Vector2 source, Vector2 target, Location location) {
        // this will hold an index into the vector of walls
        NaturalVector2 closestWall = null;
        float minOvershoot = 0;
        
        // examine each feeler in turn
        int startX = (int) Math.floor(Math.min(source.x, target.x));
        int startY = (int) Math.floor(Math.min(source.y, target.y));
        int endX = (int) Math.ceil(Math.max(source.x, target.x));
        int endY = (int) Math.ceil(Math.max(source.y, target.y));
        Array<Rectangle> tiles = location.getTiles(startX, startY, endX, endY);
        
        Vector2 tmp = new Vector2();
        Vector2 tmpDisplace = new Vector2();
        for (Rectangle tile : tiles) {
            float r = Math.max(tile.width, tile.height);
            Vector2 center = tile.getCenter(tmp);
            
            float overshoot = Intersector
                    .intersectSegmentCircleDisplace(source, target, center, r, tmpDisplace);
            if (overshoot < Float.POSITIVE_INFINITY) {
                // intersection detected
                if (closestWall == null || overshoot < minOvershoot) {
                    closestWall = NaturalVector2.of((int) tile.x, (int) tile.y);
                    minOvershoot = overshoot;
                }
            }
        }
        
        Vector2 steeringForce = null;
        if (closestWall != null) {
            steeringForce = npc.getBackwardVector().scl(minOvershoot * 5);
        }
        return steeringForce;
    }

    private void resetVelocity() {
        npc.setVelocity(velocity.x, velocity.y);
    }
    
    private void bound(Vector2 vector, float tol) {
        vector.x = Math.min(Math.max(vector.x, -tol), tol);
        vector.y = Math.min(Math.max(vector.y, -tol), tol);
    }
}
