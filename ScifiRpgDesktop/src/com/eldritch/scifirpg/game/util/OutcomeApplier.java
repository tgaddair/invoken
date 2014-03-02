package com.eldritch.scifirpg.game.util;

import java.util.List;

import com.eldritch.scifirpg.game.model.EncounterModel;
import com.eldritch.scifirpg.game.model.GameState;
import com.eldritch.scifirpg.game.model.actor.Player;
import com.eldritch.scifirpg.proto.Disciplines.Discipline;
import com.eldritch.scifirpg.proto.Outcomes.Outcome;

public class OutcomeApplier {
    private final EncounterModel<?> model;
    
    public OutcomeApplier(EncounterModel<?> model) {
        this.model = model;
    }
    
    public void apply(List<Outcome> outcomes, GameState state) {
        for (Outcome outcome : outcomes) {
            apply(outcome, state);
        }
    }

    public void apply(Outcome outcome, GameState state) {
        // Check if the outcome succeeds its random chance
        if (outcome.getWeight() >= 1.0 || outcome.getWeight() <= Math.random()) {
            Player player = state.getActorModel().getPlayer();
            switch (outcome.getType()) {
                // NOTE: if removing an equipped item would drop the count to 0, it
                // will also be unequipped
                case ITEM_CHANGE: { // COUNT of TARGET item
                    String itemId = outcome.getTarget();
                    int count = outcome.getValue();
                    player.changeItemCount(itemId, count);
                    break;
                }
                case REP_CHANGE: { // COUNT reputation with TARGET faction
                    String factionId = outcome.getTarget();
                    int value = outcome.getValue();
                    player.changeReputation(factionId, value);
                    break;
                }
                case XP_GAIN: { // COUNT experience points for TARGET discipline
                    Discipline d = Discipline.valueOf(outcome.getTarget());
                    int xp = outcome.getValue();
                    player.gainExperience(d, xp);
                    break;
                }
                case AUG_USE: { // COUNT uses of TARGET augmentation
                    break;
                }
                case AUG_GAIN: { // TARGET augmentation added to inventory
                    break;
                }
                case TELEPORT: { // TARGET destination
                    model.teleport(outcome.getTarget());
                    break;
                }
                case NEXT_ENCOUNTER: { // Set successor encounter
                    model.setSuccessor(outcome.getTarget());
                    break;
                }
                case HP_CHANGE: { // COUNT change in player health
                    player.changeHealth(outcome.getValue());
                    break;
                }
                case MISSION_SET: { // Set mission stage to TARGET
                    // TODO
                    break;
                }

                // Dialogue
                case INFLUENCE_MOD: { // COUNT change in current dialogue
                                      // disposition
                    break;
                }
                case INFLUENCE_RESET: { // Reset dialogue disposition variable
                                        // to its initial state
                    break;
                }
                case START_COMBAT: { // Only applies to dialogue in actor
                                     // encounter
                    break;
                }

                // Add and remove state markers for tracking the state of
                // specific events.
                case ADD_MARKER: {
                    break;
                }
                case REMOVE_MARKER: { // value of -1 means remove all such
                                      // markers
                    break;
                }

                case KILL: { // Kill the TARGET actor
                    break;
                }
                case GAIN_FOLLOWER: {
                    break;
                }
                case LOSE_FOLLOWER: {
                    break;
                }
                default:
                    throw new IllegalArgumentException("Unrecognized Outcome type: "
                            + outcome.getType());
            }
        }
    }
}
