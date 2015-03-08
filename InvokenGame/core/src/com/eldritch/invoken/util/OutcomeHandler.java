package com.eldritch.invoken.util;

import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.proto.Actors.DialogueTree.Choice;
import com.eldritch.invoken.proto.Actors.DialogueTree.Response;

public abstract class OutcomeHandler {
    public void handle(Response response, Agent interactor) {
        if (response.getForced() || response.getUnique()) {
            interactor.addDialogue(getId(response));
        }
    }
    
    public void handle(Choice choice, Agent interactor) {
    }
    
    protected abstract String getId(Response response);
}
