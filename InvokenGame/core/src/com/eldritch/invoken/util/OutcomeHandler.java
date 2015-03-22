package com.eldritch.invoken.util;

import java.util.Map;
import java.util.Map.Entry;

import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.factions.Faction;
import com.eldritch.invoken.actor.items.Item;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.proto.Actors.DialogueTree.Choice;
import com.eldritch.invoken.proto.Actors.DialogueTree.Response;
import com.eldritch.invoken.proto.Items.Item.Type;
import com.eldritch.invoken.proto.Outcomes.Outcome;

public abstract class OutcomeHandler {
    public void handle(Response response, Agent interactor) {
        if (response.getForced() || response.getUnique()) {
            interactor.addDialogue(getId(response));
        }

        for (Outcome outcome : response.getOutcomeList()) {
            handle(outcome, interactor, response);
        }
    }

    public void handle(Choice choice, Agent interactor) {
    }

    private void handle(Outcome outcome, Agent target, Response response) {
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
            case ITEM_TRANSFER: {
                Agent from = outcome.getValue() >= 0 ? source : target;
                Agent to = outcome.getValue() >= 0 ? target : source;

                Item item = from.getInventory().getItem(outcome.getTarget());
                int count = from.getInventory().removeItem(item, outcome.getValue());
                to.getInventory().addItem(item, count);
                break;
            }
            case ITEM_TRANSFER_ALL: {
                // default is to transfer from the target to the source
                // a positive value indicates source to target
                Agent from = outcome.hasValue() && outcome.getValue() > 0 ? source : target;
                Agent to = outcome.hasValue() && outcome.getValue() > 0 ? target : source;
                
                Type itemType = Type.valueOf(outcome.getTarget());
                Map<Item, Integer> items = from.getInventory().getItemCounts(itemType);
                for (Entry<Item, Integer> entry : items.entrySet()) {
                    int count = from.getInventory().removeItem(entry.getKey(), entry.getValue());
                    to.getInventory().addItem(entry.getKey(), count);
                }
                break;
            }
            case REP_CHANGE: // change TARGET reputation by VALUE
                Faction faction = Faction.of(outcome.getTarget());
                target.getInfo().getFactionManager()
                        .modifyReputationFor(faction, outcome.getValue());
                break;
            case RELATION_CHANGE:
                source.changeRelation(target, outcome.getValue());
                break;
            case START_COMBAT:
                source.getThreat().addEnemy(target);
                break;
            case NO_REPEAT:
                target.addDialogue(getId(response));
                break;
            case ADD_MARKER:
                target.getLocation().addMarker(outcome.getTarget(), outcome.getValue());
                break;
            default:
                throw new IllegalArgumentException("Unrecognized Outcome type: "
                        + outcome.getType());
        }
    }

    protected abstract Agent getSource();

    protected abstract String getId(Response response);
}
