package com.eldritch.invoken.util;

import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.factions.Faction;
import com.eldritch.invoken.actor.items.Item;
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
            case ITEM_CHANGE:
                if (outcome.getValue() > 0) {
                    // add items
                    Item item = Item.fromProto(InvokenGame.ITEM_READER.readAsset(outcome
                            .getTarget()));
                    target.getInfo().getInventory().addItem(item, outcome.getValue());
                } else {
                    // remove items
                    target.getInfo().getInventory()
                            .removeItem(outcome.getTarget(), outcome.getValue());
                }
                break;
            case ITEM_TRANSFER:
                Agent from;
                Agent to;
                if (outcome.getValue() > 0) {
                    // transfer the items from source to target
                    from = source;
                    to = target;
                } else {
                    // transfer the items from target to source
                    from = target;
                    to = source;
                }

                Item item = from.getInventory().getItem(outcome.getTarget());
                int count = from.getInventory().removeItem(item, outcome.getValue());
                to.getInventory().addItem(item, count);
            case REP_CHANGE: // change TARGET reputation by VALUE
                Faction faction = Faction.of(outcome.getTarget());
                target.getInfo().getFactionManager()
                        .modifyReputationFor(faction, outcome.getValue());
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
