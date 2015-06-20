package com.eldritch.invoken.actor;

import com.eldritch.invoken.location.Level;

public interface Entity extends Drawable {
    void update(float delta, Level level);
}
