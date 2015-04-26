package com.eldritch.invoken.util;

import com.eldritch.invoken.proto.Actors.PlayerActor;
import com.google.common.base.Optional;

public interface GameTransition {
    void transition(String locationName, Optional<String> encounterName, PlayerActor state);
}