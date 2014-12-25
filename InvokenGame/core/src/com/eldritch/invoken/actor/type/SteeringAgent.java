package com.eldritch.invoken.actor.type;

import java.util.Map;

import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.proto.Actors.ActorParams;

public abstract class SteeringAgent extends Agent implements Steerable<Vector2> {
	private static final SteeringAcceleration<Vector2> steeringOutput = 
	        new SteeringAcceleration<Vector2>(new Vector2());
	
	float orientation;
    float angularVelocity;
    boolean independentFacing = true;
    boolean tagged;
    SteeringBehavior<Vector2> steeringBehavior;
    
    private float maxAngularAcceleration = 10;
    private float maxAngularVelocity = 10;
    private float maxLinearAcceleration = 10;
    private float maxLinearVelocity = 10;
	
	public SteeringAgent(ActorParams params, float x, float y, float width, float height,
            Map<Activity, Map<Direction, Animation>> animations) {
		super(params, x, y, width, height, animations);
	}
	
	protected void setBehavior(SteeringBehavior<Vector2> behavior) {
		this.steeringBehavior = behavior;
	}
	
	public void update(float delta) {
		if (steeringBehavior != null) {
	        // Calculate steering acceleration
	        steeringBehavior.calculateSteering(steeringOutput);
	
	        // Apply steering acceleration to move this agent
	        applySteering(steeringOutput, delta);
	    }
	}

	@Override
	public float getMaxAngularAcceleration() {
		return maxAngularAcceleration;
	}

	@Override
	public float getMaxAngularSpeed() {
		return maxAngularVelocity;
	}

	@Override
	public float getMaxLinearAcceleration() {
		return maxLinearAcceleration;
	}

	@Override
	public float getMaxLinearSpeed() {
		return maxLinearVelocity;
	}

	@Override
	public void setMaxAngularAcceleration(float acceleration) {
		this.maxAngularAcceleration = acceleration;
	}

	@Override
	public void setMaxAngularSpeed(float speed) {
		this.maxAngularVelocity = speed;
	}

	@Override
	public void setMaxLinearAcceleration(float acceleration) {
		this.maxLinearAcceleration = acceleration;
	}

	@Override
	public void setMaxLinearSpeed(float speed) {
		this.maxLinearVelocity = speed;
	}

	// Here we assume the y-axis is pointing upwards.
	@Override
	public Vector2 angleToVector(Vector2 outVector, float angle) {
		outVector.x = -(float) Math.sin(angle);
        outVector.y = (float) Math.cos(angle);
        return outVector;
	}

	@Override
	public float getAngularVelocity() {
		return angularVelocity;
	}

	@Override
	public Vector2 getLinearVelocity() {
		return velocity;
	}

	@Override
	public float getOrientation() {
		return orientation;
	}

	@Override
	public boolean isTagged() {
		return tagged;
	}

	@Override
	public Vector2 newVector() {
		return new Vector2();
	}

	@Override
	public void setTagged(boolean tagged) {
		this.tagged = tagged;
	}

	// Here we assume the y-axis is pointing upwards.
	@Override
	public float vectorToAngle(Vector2 vector) {
		return (float) Math.atan2(-vector.x, vector.y);
	}
	
	private void applySteering(SteeringAcceleration<Vector2> steering, float time) {
        // Update position and linear velocity. Velocity is trimmed to maximum speed
        this.position.mulAdd(velocity, time);
        this.velocity.mulAdd(steering.linear, time).limit(this.getMaxLinearSpeed());

        // Update orientation and angular velocity
        if (independentFacing) {
            this.orientation += angularVelocity * time;
            this.angularVelocity += steering.angular * time;
        } else {
            // For non-independent facing we have to align orientation to linear velocity
            float newOrientation = calculateOrientationFromLinearVelocity(this);
            if (newOrientation != this.orientation) {
                this.angularVelocity = (newOrientation - this.orientation) * time;
                this.orientation = newOrientation;
            }
        }
    }
	
	public static float calculateOrientationFromLinearVelocity(Steerable<Vector2> character) {
        // If we haven't got any velocity, then we can do nothing.
        if (character.getLinearVelocity().isZero(MathUtils.FLOAT_ROUNDING_ERROR))
            return character.getOrientation();

        return character.vectorToAngle(character.getLinearVelocity());
    }
}
