package com.eldritch.scifirpg.game.model.actor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.eldritch.scifirpg.game.model.ActionAugmentation;
import com.eldritch.scifirpg.game.model.EncounterListener.ActorEncounterListener;
import com.eldritch.scifirpg.game.model.EncounterModel;
import com.eldritch.scifirpg.game.model.GameState;
import com.eldritch.scifirpg.game.util.Result;
import com.google.common.collect.Iterables;

/**
 * Handles the internal state of a single ActorEncounter. Makes requests to the
 * global ActorModel for data and updates the ActorModel's persistent state
 * (who's alive, etc.).
 * 
 */
public class ActorEncounterModel extends EncounterModel<ActorEncounter, ActorEncounterListener> {
    private static final int MAX_ACTIONS = 2;
    private final ActorModel model;
    private final List<Npc> npcs;
    
    // Combat data
    private final List<Actor> combatants;
    private final Iterator<Actor> turns;
    private Actor current;
    private int actions = 0; // How many actions the actor has taken this turn
    
    // When in combat, no dialogue or other interaction modes can take place until resolved.
    // Resolution occurs when there are no Actors in the encounter hostile to another
    // actor.  If no one is hostile to the player, they can choose to "pass" on their attack
    // turn.
    private boolean inCombat = false;
    
    // Effects
    private Set<Actor> scanned = new HashSet<Actor>();

    public ActorEncounterModel(ActorEncounter encounter, GameState state) {
        super(encounter, state);
        this.model = state.getActorModel();
        this.npcs = model.getActorsFor(getEncounter());
        
        combatants = new ArrayList<>();
        combatants.addAll(npcs);
        combatants.add(model.getPlayer());
        Collections.sort(combatants, new Comparator<Actor>() {
            @Override
            public int compare(Actor a1, Actor a2) {
                // Descending order by initiative
                return Integer.compare(a2.getInitiative(), a1.getInitiative());
            }
        });
        this.turns = Iterables.cycle(combatants).iterator();
        current = this.turns.next();
    }
    
    public void init() {
        Player player = model.getPlayer();
        player.resetHealth();
        scanned.add(player);
    }
    
    @Override
    public boolean canContinue() {
        for (Npc actor : npcs) {
            if (actor.isAlive()) {
                if (actor.hasEnemy(model.getPlayer())) {
                    // Cannot continue if someone is in combat with the player
                    return false;
                }
                if (actor.isBlocking()) {
                    // Cannot continue if a blocking NPC is alive
                    return false;
                }
            }
        }
        return true;
    }
    
    private boolean checkHostility() {
        boolean hasHostile = false;
        for (Npc actor : npcs) {
            if (actor.isAlive()) {
                if (actor.hasEnemy()) {
                    hasHostile = true;
                }
            } else {
                onActorKilled(actor);
            }
        }
        
        // Also update continue state
        boolean continuable = canContinue();
        for (ActorEncounterListener listener : getListeners()) {
            listener.canContinue(continuable);
        }
        
        return hasHostile;
    }
    
    private void onActorKilled(Npc actor) {
        model.markDead(actor.getId());
        for (ActorEncounterListener listener : getListeners()) {
            listener.actorKilled(actor);
        }
        
        // Apply outcomes on NPC death
        applyOutcomes(actor.getDeathOutcomes());
    }
    
    /**
     * Invoke on self if target is not specified.  Some augmentations will also automatically
     * apply to a specific target or group of targets (like all) depending on range.
     */
    public void invoke(ActionAugmentation aug) {
        if (!aug.needsTarget()) {
            invoke(aug, aug.getOwner());
        }
    }

    public void invoke(ActionAugmentation aug, Actor selected) {
        if (canTakeAction(aug.getOwner())) {
            // Apply the augmentation
            List<Result> results = aug.apply(combatants, selected);
            for (Result result : results) {
                for (ActorEncounterListener listener : getListeners()) {
                    listener.effectApplied(result);
                }
            }
            
            // Handle any actor that might have died in this exchange, and recheck hostilities
            // to determine if combat mode is to continue
            boolean hasHostile = checkHostility();
            
            // Notify all the listeners of the aug's use
            for (ActorEncounterListener listener : getListeners()) {
                listener.actionUsed(aug);
            }
            
            // Check for player death
            if (!model.getPlayer().isAlive()) {
                for (ActorEncounterListener listener : getListeners()) {
                    listener.playerKilled();
                }
                return;
            }
            
            // Update combat state
            if (inCombat) {
                if (!hasHostile) {
                    // If no hostilities were found, then we're not in combat
                    inCombat = false;
                    for (ActorEncounterListener listener : getListeners()) {
                        listener.endedCombat();
                    }
                } else {
                    actions++;
                    if (actions >= MAX_ACTIONS) {
                        nextCombatant();
                    } else {
                        takeCombatTurn();
                    }
                }
            } else if (hasHostile) {
                startCombat();
            }
        }
    }
    
    /**
     * Returns true iff it's the Actor's turn and they have actions remaining.
     */
    private boolean canTakeAction(Actor actor) {
        if (!inCombat) {
            return true;
        }
        return current == actor && actions < MAX_ACTIONS;
    }
    
    public void startCombat() {
        if (!inCombat) {
            inCombat = true;
            for (ActorEncounterListener listener : getListeners()) {
                listener.startedCombat();
            }
            for (ActorEncounterListener listener : getListeners()) {
                listener.combatTurnStarted(current);
            }
            startCombatTurn();
        }
    }
    
    public void passCombat() {
        for (ActorEncounterListener listener : getListeners()) {
            listener.combatTurnPassed(current);
        }
        nextCombatant();
    }
    
    private void nextCombatant() {
        boolean found = false;
        while (turns.hasNext() && !found) {
            current = turns.next();
            if (current.isAlive()) {
                found = true;
            } else {
                turns.remove();
            }
        }
        
        if (found) {
            actions = 0;
            for (ActorEncounterListener listener : getListeners()) {
                listener.combatTurnStarted(current);
            }
            
            startCombatTurn();
        } else {
            // Somehow every actor is dead, so end combat
        }
    }
    
    private void startCombatTurn() {
        takeCombatTurn();
    }
    
    private void takeCombatTurn() {
        current.takeCombatTurn(this);
    }

    public boolean isInCombat() {
        return inCombat;
    }
    
    public Player getPlayer() {
        return model.getPlayer();
    }
    
    public List<Npc> getActors() {
        return npcs;
    }
    
    public ActorModel getActorModel() {
        return model;
    }
}
