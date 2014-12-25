package com.eldritch.invoken.actor.ai;

import com.badlogic.gdx.ai.steer.utils.Collision;
import com.badlogic.gdx.ai.steer.utils.Ray;
import com.badlogic.gdx.ai.steer.utils.RaycastCollisionDetector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Npc;
import com.eldritch.invoken.encounter.Location;

public class LocationCollisionDetector implements RaycastCollisionDetector<Vector2> {
	private final Npc npc;
	private final Location location;
	
	public LocationCollisionDetector(Npc npc, Location location) {
		this.npc = npc;
		this.location = location;
	}
	
	@Override
	public boolean findCollision(Collision<Vector2> outputCollision, Ray<Vector2> inputArray) {
		Vector2 start = inputArray.origin;
		Vector2 end = inputArray.origin.cpy().add(inputArray.direction);
		if (!start.epsilonEquals(end, MathUtils.FLOAT_ROUNDING_ERROR)) {
			boolean collision = location.collides(start, end, outputCollision);
			return collision;
		}
		return false;
	}
}
