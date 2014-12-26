package com.eldritch.invoken.actor.type;

import java.util.Map;

import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.eldritch.invoken.actor.Profession;
import com.eldritch.invoken.proto.Actors.ActorParams;

public abstract class SteeringAgent extends Agent implements Steerable<Vector2> {
	protected static final SteeringAcceleration<Vector2> steeringOutput = 
	        new SteeringAcceleration<Vector2>(new Vector2());
	
	float orientation;
    Vector2 linearVelocity = new Vector2();
    float angularVelocity;
    boolean independentFacing = true;
    boolean tagged;
    SteeringBehavior<Vector2> steeringBehavior;
    
    private float maxAngularAcceleration = 1;
    private float maxAngularVelocity = 1;
    private float maxLinearAcceleration = 10;
    private float maxLinearVelocity = 5;
	
	public SteeringAgent(ActorParams params, float x, float y, float width, float height,
            World world, Map<Activity, Map<Direction, Animation>> animations) {
		super(params, x, y, width, height, world, animations);
	}
	
	public SteeringAgent(float x, float y, float width, float height, Profession profession, int level,
            World world, Map<Activity, Map<Direction, Animation>> animations) {
		super(x, y, width, height, profession, level, world, animations);
	}
	
	protected void setBehavior(SteeringBehavior<Vector2> behavior) {
		this.steeringBehavior = behavior;
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
		return body.getAngularVelocity();
	}

	@Override
	public Vector2 getLinearVelocity() {
		return body.getLinearVelocity();
	}

	@Override
	public float getOrientation() {
		return body.getAngle();
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
	
	protected void applySteering(SteeringAcceleration<Vector2> steering, float deltaTime) {
		boolean anyAccelerations = false;
		
		float scale = deltaTime * 50;

		// Update position and linear velocity.
		if (!steeringOutput.linear.isZero()) {
			Vector2 force = steeringOutput.linear.scl(scale);
			body.applyForceToCenter(force, true);
			anyAccelerations = true;
		}

		// Update orientation and angular velocity
		if (independentFacing) {
			if (steeringOutput.angular != 0) {
				body.applyTorque(steeringOutput.angular * scale, true);
				anyAccelerations = true;
			}
		}
		else {
			// If we haven't got any velocity, then we can do nothing.
			Vector2 linVel = getLinearVelocity();
			if (!linVel.isZero(MathUtils.FLOAT_ROUNDING_ERROR)) {
				float newOrientation = vectorToAngle(linVel);
				body.setAngularVelocity((newOrientation - getAngularVelocity()) * scale); // this is superfluous if independentFacing is always true
				body.setTransform(body.getPosition(), newOrientation);
			}
		}

		if (anyAccelerations) {
			// body.activate();

			// TODO:
			// Looks like truncating speeds here after applying forces doesn't work as expected.
			// We should likely cap speeds form inside an InternalTickCallback, see
			// http://www.bulletphysics.org/mediawiki-1.5.8/index.php/Simulation_Tick_Callbacks

			// Cap the linear speed
			Vector2 velocity = body.getLinearVelocity();
			float currentSpeedSquare = velocity.len2();
			float maxLinearSpeed = getMaxLinearSpeed();
			if (currentSpeedSquare > maxLinearSpeed * maxLinearSpeed) {
				body.setLinearVelocity(velocity.scl(maxLinearSpeed / (float) Math.sqrt(currentSpeedSquare)));
//				body.setLinearVelocity(body.getLinearVelocity().cpy().clamp(0, maxLinearSpeed));
			}
			
			// Cap the angular speed
			float maxAngVelocity = getMaxAngularSpeed();
			if (body.getAngularVelocity() > maxAngVelocity) {
				body.setAngularVelocity(maxAngVelocity);
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
