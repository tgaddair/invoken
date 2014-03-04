package com.eldritch.scifirpg.game.util;

import com.eldritch.scifirpg.game.model.actor.Actor;

public class Result {
    private final Actor actor;
    private final String message;
    private final boolean success;
    
    public Result(Actor actor, String message) {
        this(actor, message, true);
    }
    
    public Result(Actor actor, String message, boolean success) {
        this.actor = actor;
        this.message = message;
        this.success = success;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public Actor getActor() {
        return actor;
    }
    
    @Override
    public String toString() {
        return message;
    }
    
    public void process() {
        // Does nothing
    }
}