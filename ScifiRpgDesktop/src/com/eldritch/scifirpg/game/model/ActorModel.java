package com.eldritch.scifirpg.game.model;

import com.eldritch.scifirpg.game.util.ActorMarshaller;
import com.eldritch.scifirpg.proto.Actors.NonPlayerActor;

public class ActorModel {
    private final ActorMarshaller actorMarshaller = new ActorMarshaller();
    
    public NonPlayerActor getActor(String id) {
        return actorMarshaller.readAsset(id);
    }
}
