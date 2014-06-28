package com.eldritch.invoken.actor.pathfinding;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.encounter.Location;

public interface SteeringBehavior {
    Vector2 getForce(Location location);
    
    double getPriority();
    
    void setEnabled(boolean enabled);
    
    boolean isEnabled();
}