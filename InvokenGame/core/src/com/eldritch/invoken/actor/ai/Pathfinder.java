package com.eldritch.invoken.actor.ai;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.screens.GameScreen;

public class Pathfinder {
	private final Vector2 temp = new Vector2();
	private Vector2 target = null;
	
	public void reset() {
		target = null;
	}

	public Vector2 getTarget(Vector2 origin, Vector2 destination,
			GameScreen screen) {
		if (target == null || origin.dst2(target) < 1) {
			target = destination;
		}
		
		boolean valid = false;
		double angle = Math.PI / 4;
		angle *= Math.random() < 0.5 ? -1 : 1;
		while (!valid && angle < 2 * Math.PI && angle > -2 * Math.PI) {
			temp.x = target.x;
			temp.y = target.y;
			temp.sub(origin).nor();

			valid = isClear(origin, temp, screen);
			if (!valid) {
				target = rotate(target, origin, angle);
				angle *= -2;
			}
		}
		
		if (!valid) {
			// never found a decent path
			target = null;
			return origin;
		}

		return target;
	}

	private boolean isClear(Vector2 origin, Vector2 unit, GameScreen screen) {
		float x = origin.x;
		float y = origin.y;
		for (int i = 0; i < 5; i++) {
			x += unit.x;
			y += unit.y;
			if (screen.isObstacle((int) x, (int) y)) {
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
		Vector2 newTarget = new Vector2(
				result.x * c - result.y * s, result.x * s + result.y * c);

		// translate point back:
		result = newTarget.add(pivot);
		return result;
	}
}
