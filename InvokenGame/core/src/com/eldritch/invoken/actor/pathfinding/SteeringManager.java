package com.eldritch.invoken.actor.pathfinding;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.encounter.Location;

public class SteeringManager {
    private final Agent agent;
    private final float maxForce;
    private final List<SteeringBehavior> behaviors;
    
    public SteeringManager(Agent agent, List<SteeringBehavior> behaviors) {
        this.agent = agent;
        this.maxForce = agent.getMaxVelocity();
        this.behaviors = behaviors;
        Collections.sort(behaviors, new Comparator<SteeringBehavior>() {
            @Override
            public int compare(SteeringBehavior b1, SteeringBehavior b2) {
                return Double.compare(b2.getPriority(), b1.getPriority());
            }
        });
    }
    
    public void steer(Vector2 target, Location location) {
        Vector2 force = getForce(target, location);
        agent.applyForce(force);
    }
    
    private Vector2 getForce(Vector2 target, Location location) {
        Vector2 steeringForce = new Vector2();
        
        for (SteeringBehavior behavior : behaviors) {
            if (behavior.isEnabled()) {
                Vector2 force = behavior.getForce(target, location);
                if (!accumulate(steeringForce, force)) {
                    return steeringForce;
                }
            }
        }
        
        return steeringForce;
    }
    
    private boolean accumulate(Vector2 steeringForce, Vector2 force) {
        float magnitude = steeringForce.len();
        float remaining = maxForce - magnitude;
        if (remaining <= 0) {
            return false;
        }
        
        float additional = force.len();
        if (additional < remaining) {
            steeringForce.add(force);
        } else {
            steeringForce.add(force.nor().scl(remaining));
        }
        return true;
    }
}
