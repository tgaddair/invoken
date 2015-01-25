package com.eldritch.invoken.actor;

import com.eldritch.invoken.encounter.Location;

public interface Entity extends Drawable {
    void update(float delta, Location location);
}
