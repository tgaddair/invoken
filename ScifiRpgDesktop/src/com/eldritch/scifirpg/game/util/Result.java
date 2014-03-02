package com.eldritch.scifirpg.game.util;

import com.eldritch.scifirpg.game.model.actor.Actor;

public class Result {
    private final Actor actor;
    private final String message;
    
    public Result(Actor actor, String message) {
        this.actor = actor;
        this.message = message;
    }
    
    public Actor getActor() {
        return actor;
    }
    
    @Override
    public String toString() {
        return message;
    }
}