package com.eldritch.invoken.util;

import com.eldritch.invoken.actor.AgentInfo;
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
        AgentInfo info = source.getInfo();
        switch (outcome.getType()) {
            case REP_CHANGE:
                Faction faction = Faction.of(outcome.getTarget());
                info.getFactionManager().modifyReputationFor(faction, outcome.getValue());
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
