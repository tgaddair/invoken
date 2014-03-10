package com.eldritch.scifirpg.game.model.actor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.eldritch.scifirpg.game.util.Result;
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
        owner.onCombatStarted();
        
        // Choose the first valid combatant or end if none found
        startNextCombatTurn();
    }
    
    private void startNextCombatTurn() {
        if (!actorCycle.hasNext()) {
            endCombat();
            return;
        }
        
        // Reset remaining actions and apply all active status effects / counters
        current = actorCycle.next();
        if (current.isAlive() && current.startTurn()) {
            owner.onCombatTurnStarted(current.getActor());
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
                owner.onActionRequested(actor.getActor());
                actor.getActor().takeCombatTurn(this);
            }
        } else {
            endCombatTurn();
        }
    }
    
    public void passCombat(ActorState actor) {
        if (actor == current) {
            owner.onCombatPassed(actor.getActor());
            endCombatTurn();
        }
    }
    
    public void takeAction(Action action) {
        // Sanity check
        if (!canTakeAction(action)) {
            return;
        } else if (current != action.getActor()) {
            current = action.getActor();
        }
        
        doAction(action);
        
        // Cleanup
        if (combat) {
            // Continue the combat cycle
            current.markActionTaken(action);
            if (current.hasActions()) {
                startCombatAction(current);
            } else {
                endCombatTurn();
            }
        } else if (checkHostility()) {
            // Start the combat cycle
            startCombat();
        }
    }
    
    private boolean canTakeAction(Action action) {
        if (combat && action.getActor() != current) {
            return false;
        }
        return action.canTake();
    }
    
    private void doAction(Action action) {
        // If action is counter and selected is absent: add counter to counters
        
        // Handle counter
        
        // Handle effects
        List<Result> results = action.applyEffects(actors);
        owner.onResults(results);
        
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
