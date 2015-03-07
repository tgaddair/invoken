package com.eldritch.invoken.actor.type;

import com.badlogic.gdx.math.Vector2;

public interface HandledProjectile {
    Agent getOwner();
    
    void apply(Agent target);
    
    void reset(Agent owner, Vector2 target);
    
    float getDamage(Agent target);
    
    void cancel();
    
    public static interface ProjectileHandler {
        boolean handle(HandledProjectile projectile);
    }
}
