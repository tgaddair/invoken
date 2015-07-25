package com.eldritch.invoken.actor.ai;

import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.utils.rays.CentralRayWithWhiskersConfiguration;
import com.badlogic.gdx.ai.utils.Ray;
import com.badlogic.gdx.math.Vector;

public class AdaptiveRayWithWhiskersConfiguration<T extends Vector<T>> extends CentralRayWithWhiskersConfiguration<T> {
    private static final float MAX_LINEAR_SPEED = 15f;
    
	private final float maxRayLength;
	private final float maxWhiskerLength;
	private final float maxWhiskerAngle;

	public AdaptiveRayWithWhiskersConfiguration(Steerable<T> owner,
			float maxRayLength, float maxWhiskerLength, float maxWhiskerAngle) {
		super(owner, maxRayLength, maxWhiskerLength, maxWhiskerAngle);
		this.maxRayLength = maxRayLength;
		this.maxWhiskerLength = maxWhiskerLength;
		this.maxWhiskerAngle = maxWhiskerAngle;
	}
	
	@Override
	public Ray<T>[] updateRays() {
		// Scale the whisker angle down to 0 as the owner's linear velocity approaches the max.
		float velocity = owner.getLinearVelocity().len2();
		float maxVelocity = MAX_LINEAR_SPEED * MAX_LINEAR_SPEED;
		
		float angle = ((maxVelocity - velocity) / maxVelocity) * maxWhiskerAngle;
		setWhiskerAngle(angle);
		
		float whiskerLength = (velocity / maxVelocity) * maxWhiskerLength;
		whiskerLength = Math.min(whiskerLength + 0.5f, maxWhiskerLength);
		setWhiskerLength(whiskerLength);
		
		float length = (velocity / maxVelocity) * maxRayLength;
		length = Math.min(length + 0.75f, maxRayLength);
		setRayLength(length);
		
		return super.updateRays();
	}

}
