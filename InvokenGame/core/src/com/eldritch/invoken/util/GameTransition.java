package com.eldritch.invoken.util;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.eldritch.invoken.proto.Actors.PlayerActor;

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
    
    public void transition(GameState prev, GameState next, PlayerActor playerState) {
        handler.transition(prev, next, playerState);
    }
    
    public static class GameState {
        private final String region;
        private final int floor;
        
        public GameState(String region, int floor) {
            this.region = region;
            this.floor = floor;
        }

        public String getRegion() {
            return region;
        }

        public int getFloor() {
            return floor;
        }
    }
    
    public interface GameTransitionHandler {
        void transition(GameState prev, GameState next, PlayerActor playerState);
    }
}