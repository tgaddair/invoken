package com.eldritch.invoken.actor.ai;

import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.utils.Ray;
import com.badlogic.gdx.ai.steer.utils.rays.CentralRayWithWhiskersConfiguration;
import com.badlogic.gdx.math.Vector;

public class AdaptiveRayWithWhiskersConfiguration<T extends Vector<T>> extends CentralRayWithWhiskersConfiguration<T> {
	private final float maxRayLength;
	private final float maxWhiskerAngle;

	public AdaptiveRayWithWhiskersConfiguration(Steerable<T> owner,
			float maxRayLength, float whiskerLength, float maxWhiskerAngle) {
		super(owner, maxRayLength, whiskerLength, maxWhiskerAngle);
		this.maxRayLength = maxRayLength;
		this.maxWhiskerAngle = maxWhiskerAngle;
	}
	
	@Override
	public Ray<T>[] updateRays() {
		// Scale the whisker angle down to 0 as the owner's linear velocity approaches the max.
		float velocity = owner.getLinearVelocity().len2();
		float maxVelocity = owner.getMaxLinearSpeed() * owner.getMaxLinearSpeed();
		
		float angle = ((maxVelocity - velocity) / maxVelocity) * maxWhiskerAngle;
		setWhiskerAngle(angle);
		
		float length = (velocity / maxVelocity) * maxRayLength;
		length = Math.min(length + 1, maxRayLength);
		setRayLength(length);
		
		return super.updateRays();
	}

}