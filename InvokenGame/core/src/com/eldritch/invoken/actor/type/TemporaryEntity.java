package com.eldritch.invoken.actor.type;

import com.eldritch.invoken.actor.Entity;

public interface TemporaryEntity extends Entity {
    boolean isFinished();
    
    void dispose();
}
