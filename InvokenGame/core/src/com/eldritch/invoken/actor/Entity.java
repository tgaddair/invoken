package com.eldritch.invoken.actor;

import com.eldritch.invoken.location.Location;

public interface Entity extends Drawable {
    void update(float delta, Location location);
}
