package com.eldritch.invoken.actor.ai;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.encounter.Location;

public class Pathfinder {
	private final Vector2 temp = new Vector2();
	private Vector2 target = null;
	
	public void reset() {
		target = null;
	}

	public Vector2 getTarget(Vector2 origin, Vector2 destination,
			Location screen) {
		if (target == null || origin.dst2(target) < 2) {
			// keep pursuing our previous target until we reach it
			target = destination;
		}
		
		// find a valid target
		boolean valid = false;
		double angle = Math.PI / 4;
		angle *= Math.random() < 0.5 ? -1 : 1;
		while (!valid && angle < 2 * Math.PI && angle > -2 * Math.PI) {
			valid = isClear(origin, target, screen);
			if (!valid) {
				// obstacle in the way, so rotate in the opposite direction
				target = rotate(target, origin, angle);
				angle *= -2;
			}
		}
		
		if (!valid) {
			// never found a decent path
			target = null;
			return origin;
		}

		// found a valid path
		return target;
	}
	
	/**
	 * Returns true if the path from origin to destination is clear (no obstacles) with respect to
	 * the location.
	 */
	public boolean isClear(Vector2 origin, Vector2 destination, Location location) {
	    // define our unit of movement for checking obstacle collisions
	    Vector2 unit = temp;
        temp.x = destination.x;
        temp.y = destination.y;
        temp.sub(origin).nor();
        
		float x = origin.x;
		float y = origin.y;
		for (int i = 0; i < 5; i++) {
			x += unit.x;
			y += unit.y;
			if (location.isObstacle((int) x, (int) y)) {
				return false;
			}
		}
		return true;
	}

	private Vector2 rotate(Vector2 source, Vector2 pivot, double angle) {
		Vector2 result = source.cpy();
		float s = (float) Math.sin(angle);
		float c = (float) Math.cos(angle);

		// translate point back to origin:
		result = result.sub(pivot);

		// rotate point
		result.x = result.x * c - result.y * s;
		result.y = result.x * s + result.y * c;

		// translate point back:
		result = result.add(pivot);
		return result;
	}
}
