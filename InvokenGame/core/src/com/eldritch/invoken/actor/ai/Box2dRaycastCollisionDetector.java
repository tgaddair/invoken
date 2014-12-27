package com.eldritch.invoken.actor.ai;

import com.badlogic.gdx.ai.steer.utils.Collision;
import com.badlogic.gdx.ai.steer.utils.Ray;
import com.badlogic.gdx.ai.steer.utils.RaycastCollisionDetector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;

public class Box2dRaycastCollisionDetector implements RaycastCollisionDetector<Vector2> {
	private final World world;
	private final Box2dRaycastCallback callback;
	private final Vector2 end = new Vector2();

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
		end.set(inputRay.origin).add(inputRay.direction);
		if (!inputRay.origin.epsilonEquals(end, MathUtils.FLOAT_ROUNDING_ERROR)) {
			callback.outputCollision = outputCollision;
			world.rayCast(callback, inputRay.origin, end);
		}
		return callback.collided;
	}

	public static class Box2dRaycastCallback implements RayCastCallback {
		public Collision<Vector2> outputCollision;
		public boolean collided;

		@Override
		public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
			if (outputCollision != null) outputCollision.set(point, normal);
			collided = true;
			return fraction;
		}
	}
}
