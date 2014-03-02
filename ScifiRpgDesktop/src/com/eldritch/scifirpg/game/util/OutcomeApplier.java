package com.eldritch.scifirpg.game.util;

import java.util.List;

import com.eldritch.scifirpg.game.model.GameState;
import com.eldritch.scifirpg.game.model.actor.Player;
import com.eldritch.scifirpg.proto.Outcomes.Outcome;

public class OutcomeApplier {
    public void apply(List<Outcome> outcomes, GameState state) {
        for (Outcome outcome : outcomes) {
            apply(outcome, state);
        }
    }
    
    public void apply(Outcome outcome, GameState state) {
        Player player = state.getActorModel().getPlayer();
        switch (outcome.getType()) {
            // NOTE: if removing an equipped item would drop the count to 0, it will also be unequipped
            case ITEM_CHANGE: { // COUNT of TARGET item
            }
            case REP_CHANGE: { // COUNT reputation with TARGET faction
                
            }
            case XP_GAIN: { // COUNT experience points for TARGET discipline
                
            }
            case AUG_USE: {  // COUNT uses of TARGET augmentation
                
            }
            case AUG_GAIN: {  // TARGET augmentation added to inventory
                
            }
            case TELEPORT: {  // TARGET destination
                
            }
            case NEXT_ENCOUNTER: {  // Set successor encounter
                
            }
            case HP_CHANGE: {  // COUNT change in player health
                
            }
            case MISSION_SET: {  // Set mission stage to TARGET
                
            }

            // Dialogue
            case INFLUENCE_MOD: {  // COUNT change in current dialogue disposition
                
            }
            case INFLUENCE_RESET: {  // Reset dialogue disposition variable to its initial state
                
            }
            case START_COMBAT: {  // Only applies to dialogue in actor encounter
                
            }

            // Add and remove state markers for tracking the state of specific events.
            case ADD_MARKER: { 
                
            }
            case REMOVE_MARKER: {  // value of -1 means remove all such markers
                
            }

            case KILL: {  // Kill the TARGET actor
                
            }
            case GAIN_FOLLOWER: { 
                
            }
            case LOSE_FOLLOWER: { 
                
            }
            default:
                throw new IllegalArgumentException(
                        "Unrecognized Outcome type: " + outcome.getType());
        }
    }
}
