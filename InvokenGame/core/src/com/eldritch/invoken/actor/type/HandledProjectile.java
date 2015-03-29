package com.eldritch.invoken.actor.type;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.util.Damage;

public interface HandledProjectile extends TemporaryEntity {
    Agent getOwner();
    
    Damage getDamage();
    
    void apply(Agent target);
    
    void reset(Agent owner, Vector2 target);
    
    void cancel();
    
    public static interface ProjectileHandler {
        boolean handle(HandledProjectile projectile);
    }
}
