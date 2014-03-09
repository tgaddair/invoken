package com.eldritch.scifirpg.game.model.actor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Iterables;

public class ActionModel {
    private final ActorEncounterModel owner;
    private final Map<Actor, ActorState> states = new HashMap<>();
    private final List<ActorState> actors;
    private final Iterator<ActorState> actorCycle;
    private ActorState current;
    private boolean combat = false;
    
    public ActionModel(ActorEncounterModel owner) {
        this.owner = owner;
        
        actors = new ArrayList<>();
        for (Actor actor : owner.getActors()) {
            ActorState state = new ActorState(actor);
            actors.add(state);
            states.put(actor, state);
        }
        Collections.sort(actors);
        actorCycle = Iterables.cycle(actors).iterator();
    }
    
    public ActorState getState(Actor actor) {
        return states.get(actor);
    }
    
    private void startCombat() {
        combat = true;
        
        // Notify combat started
        
        // Choose the first valid combatant or end if none found
        startNextCombatTurn();
    }
    
    private void startNextCombatTurn() {
        if (!actorCycle.hasNext()) {
            endCombat();
            return;
        }
        
        current = actorCycle.next();
        if (current.isAlive()) {
            // Reset remaining actions and apply all active status effects / counters
            current.startTurn();
            
            startCombatAction(current);
        } else {
            actorCycle.remove();
            endCombatTurn();
        }
    }
    
    private void startCombatAction(ActorState actor) {
        if (actor.canTakeAnyAction()) {
            if (actor.checkPanicked()) {
                takeAction(actor.randomAction());
            } else {
                // Notify action requested
            }
        } else {
            endCombatTurn();
        }
    }
    
    public void passCombat(ActorState actor) {
        if (actor == current) {
            endCombatTurn();
        }
    }
    
    public void takeAction(Action action) {
        // Sanity check
        if (!canTakeAction(action)) {
            return;
        }
        
        // Notify action started
        
        doAction(action);
        
        // Cleanup
        if (combat) {
            // Continue the combat cycle
            current.markActionTaken(action);
            startCombatAction(current);
        } else if (checkHostility()) {
            // Start the combat cycle
            startCombat();
        } else {
            // Notify action ended
        }
    }
    
    private boolean canTakeAction(Action action) {
        if (action.getActor() != current) {
            return false;
        }
        if (!action.hasSelectedTarget()) {
            return false;
        }
        return current.canTakeAction(action);
    }
    
    private void doAction(Action action) {
        // If action is counter and selected is absent: add counter to counters
        
        // Handle counter
        
        // Handle effects
        action.applyEffects(actors);
        
        // Check if any actors were killed by this action and end the combat turn if the current
        // actor was killed somehow
        owner.checkActorsAlive();
        if (!current.isAlive()) {
            endCombatTurn();
        }
    }
    
    private void endCombatTurn() {
        if (checkHostility()) {
            // By definition, there must be a next combatant if there are still hostilities
            startNextCombatTurn();
        } else {
            endCombat();
        }
    }
    
    private void endCombat() {
        combat = false;
        // Notify combat ended
    }
    
    private boolean checkHostility() {
        return owner.checkHostility();
    }
}
