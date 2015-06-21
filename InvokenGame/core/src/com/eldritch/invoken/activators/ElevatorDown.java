package com.eldritch.invoken.activators;

import com.eldritch.invoken.location.NaturalVector2;

public class ElevatorDown extends LevelDoorActivator {
    public ElevatorDown(NaturalVector2 position) {
        super(position.x, position.y, 1);
    }
}
