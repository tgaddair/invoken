package com.eldritch.invoken.actor.ai;

import com.badlogic.gdx.ai.utils.Collision;
import com.badlogic.gdx.ai.utils.Ray;
import com.badlogic.gdx.ai.utils.RaycastCollisionDetector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.util.Settings;

public class Box2dRaycastCollisionDetector implements RaycastCollisionDetector<Vector2> {
    private final World world;
    private final Box2dRaycastCallback callback;

    public Box2dRaycastCollisionDetector(World world) {
        this(world, new Box2dRaycastCallback());
    }

    public Box2dRaycastCollisionDetector(World world, Box2dRaycastCallback callback) {
        this.world = world;
        this.callback = callback;
    }

    @Override
    public boolean findCollision(Collision<Vector2> outputCollision, Ray<Vector2> inputRay) {
        callback.collided = false;
        if (!inputRay.start.epsilonEquals(inputRay.end, MathUtils.FLOAT_ROUNDING_ERROR)) {
            callback.outputCollision = outputCollision;
            world.rayCast(callback, inputRay.start, inputRay.end);
        }
        return callback.collided;
    }

    @Override
    public boolean collides(Ray<Vector2> inputRay) {
        return findCollision(null, inputRay);
    }

    public static class Box2dRaycastCallback implements RayCastCallback {
        private final short mask = Settings.BIT_TARGETABLE;
        public Collision<Vector2> outputCollision;
        public boolean collided;

        @Override
        public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
            if (!isObstruction(fixture)) {
                // no collision
                return 0;
            }

            if (outputCollision != null)
                outputCollision.set(point, normal);
            collided = true;
            return fraction;
        }

        private boolean isObstruction(Fixture fixture) {
            short category = fixture.getFilterData().categoryBits;
            if ((mask & category) == 0) {
                // no common bits, so these items don't collide
                return false;
            }
            
            // check that the fixture belongs to another agent
            if (fixture.getUserData() != null && fixture.getUserData() instanceof Agent) {
                Agent agent = (Agent) fixture.getUserData();
                if (!agent.isAlive()) {
                    // cannot be obstructed by the body of a dead agent
                    return false;
                }
            }

            // whatever it is, it's in the way
            return true;
        }
    }
}
