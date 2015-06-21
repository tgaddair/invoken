package com.eldritch.invoken.activators;

import com.eldritch.invoken.location.NaturalVector2;

public class ElevatorUp extends LevelDoorActivator {
    public ElevatorUp(NaturalVector2 position) {
        super(position.x, position.y, -1);
    }
}
