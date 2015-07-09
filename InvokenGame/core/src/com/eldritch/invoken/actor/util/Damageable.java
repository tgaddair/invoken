package com.eldritch.invoken.actor.util;

import com.badlogic.gdx.math.Vector3;

public interface Damageable extends Locatable {
    float getBaseHealth();
    
    float getHealth();
    
    boolean isAlive();
    
    void setHealthIndicator(Vector3 worldCoords);
}
