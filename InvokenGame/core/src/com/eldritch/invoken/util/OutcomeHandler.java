package com.eldritch.invoken.util;

import com.eldritch.invoken.actor.factions.Faction;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.proto.Actors.DialogueTree.Choice;
import com.eldritch.invoken.proto.Actors.DialogueTree.Response;
import com.eldritch.invoken.proto.Outcomes.Outcome;

public abstract class OutcomeHandler {
    public void handle(Response response, Agent interactor) {
        if (response.getForced() || response.getUnique()) {
            interactor.addDialogue(getId(response));
        }
        
        for (Outcome outcome : response.getOutcomeList()) {
            handle(outcome, interactor);
        }
    }
    
    public void handle(Choice choice, Agent interactor) {
    }
    
    private void handle(Outcome outcome, Agent target) {
        Agent source = getSource();
        switch (outcome.getType()) {
            case REP_CHANGE:  // change TARGET reputation by VALUE
                Faction faction = Faction.of(outcome.getTarget());
                target.getInfo().getFactionManager().modifyReputationFor(faction, outcome.getValue());
                break;
            case RELATION_CHANGE:
                source.changeRelation(target, outcome.getValue());
                break;
            case START_COMBAT:
                source.addEnemy(target);
                break;
            default:
                throw new IllegalArgumentException("Unrecognized Outcome type: "
                        + outcome.getType());
        }
    }
    
    protected abstract Agent getSource();
    
    protected abstract String getId(Response response);
}
