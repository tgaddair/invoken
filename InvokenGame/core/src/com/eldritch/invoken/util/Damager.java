package com.eldritch.invoken.util;

import com.badlogic.gdx.math.Vector2;

public interface Damager {
    Damage getDamage();
    
    Vector2 getDirection();
}
