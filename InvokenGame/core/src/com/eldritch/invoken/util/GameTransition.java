package com.eldritch.invoken.util;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.eldritch.invoken.proto.Actors.PlayerActor;
import com.google.common.base.Optional;

public interface GameTransition {
    void transition(String locationName, Optional<String> encounterName, PlayerActor state);
    
    void transition(String region, int level, PlayerActor state);
    
    Skin getSkin();
}