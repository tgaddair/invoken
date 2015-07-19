package com.eldritch.invoken.util;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.eldritch.invoken.proto.Actors.PlayerActor;
import com.google.common.base.Optional;

public class GameTransition {
    private final GameTransitionHandler handler;
    private final Skin skin;
    
    public GameTransition(GameTransitionHandler handler, Skin skin) {
        this.handler = handler;
        this.skin = skin;
    }
    
    public Skin getSkin() {
        return skin;
    }
    
    public void transition(String locationName, Optional<String> encounterName, PlayerActor state) {
        handler.transition(locationName, encounterName, state);
    }
    
    public void transition(String region, int level, PlayerActor state) {
        handler.transition(region, level, state);
    }
    
    public interface GameTransitionHandler {
        void transition(String locationName, Optional<String> encounterName, PlayerActor state);
    
        void transition(String region, int level, PlayerActor state);
    }
}