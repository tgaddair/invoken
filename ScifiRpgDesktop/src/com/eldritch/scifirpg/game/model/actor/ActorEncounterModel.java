package com.eldritch.scifirpg.game.model.actor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.eldritch.scifirpg.game.model.ActionAugmentation;
import com.eldritch.scifirpg.game.model.EncounterListener.ActorEncounterListener;
import com.eldritch.scifirpg.game.model.EncounterModel;
import com.eldritch.scifirpg.game.model.GameState;
import com.eldritch.scifirpg.game.util.EffectUtil;
import com.eldritch.scifirpg.game.util.Result;
import com.eldritch.scifirpg.proto.Effects.Effect;
import com.google.common.base.Optional;
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
        model.getPlayer().resetHealth();
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

    public void invoke(ActionAugmentation aug, Actor target) {
        if (canTakeAction(aug.getOwner())) {
            // Allow target to respond to the invocation
            boolean success = true;
            boolean hostileAction = false;
            boolean countered = false;
            switch (aug.getType()) {
                case ATTACK: // Playable to make hostile
                    success = target.handleAttack(aug);
                    hostileAction = true;
                    break;
                case DECEIVE: // Playable when not detected
                case EXECUTE: // Playable in encounter
                case DIALOGUE: // Playable in dialogue
                case COUNTER: // Playable when targeted
                    countered = true;
                    break;
                case TRAP: // Playable at any time, activates when targeted and effect applies
                    break;
                case PASSIVE: // Playable when attuning outside encounter
                    // It's a bug if we have an ActionAugmentation with passive type
                default:
                    throw new IllegalArgumentException(
                            "Unrecognized Augmentation Type: " + aug.getType());
            }
            
            if (success) {
                // No counter, apply effects
                Optional<Actor> source = Optional.of(aug.getOwner());
                Optional<Actor> dest = Optional.of(target);
                for (Effect effect : aug.getEffects()) {
                    Result result = EffectUtil.apply(effect, source, dest);
                    for (ActorEncounterListener listener : getListeners()) {
                        listener.effectApplied(result);
                    }
                }
                
                // TODO handle duration effects by keeping a list we apply at the end of the round
            }
            
            // Handle any actor that might have died in this exchange, and recheck hostilities
            // to determine if combat mode is to continue
            boolean hasHostile = checkHostility();
            
            // Remove the augmentation from the owner's buffer and notify all the listeners
            aug.getOwner().removeAction(aug);
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
            if (inCombat && !countered) {
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
            } else if (hostileAction) {
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
        Set<ActionAugmentation> actions = current.drawActions();
        for (ActorEncounterListener listener : getListeners()) {
            listener.actionsDrawn(current, actions);
        }
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
