package com.eldritch.scifirpg.game.model.actor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.eldritch.scifirpg.game.model.ActionAugmentation;
import com.eldritch.scifirpg.game.model.EncounterModel;
import com.eldritch.scifirpg.game.util.EffectUtil;
import com.eldritch.scifirpg.game.util.Result;
import com.eldritch.scifirpg.proto.Effects.Effect;
import com.google.common.base.Optional;

/**
 * Handles the internal state of a single ActorEncounter. Makes requests to the
 * global ActorModel for data and updates the ActorModel's persistent state
 * (who's alive, etc.).
 * 
 */
public class ActorEncounterModel extends EncounterModel<ActorEncounter> {
    private static final int MAX_ACTIONS = 2;
    private final ActorModel model;
    private final List<Npc> actors;
    private final List<ActorEncounterListener> listeners = new ArrayList<>();
    private final List<Actor> combatants = new ArrayList<>();
    private int turn = 0; // Actor index whose current combat turn it is
    private int actions = 0; // How many actions the actor has taken this turn
    
    // When in combat, no dialogue or other interaction modes can take place until resolved.
    // Resolution occurs when there are no Actors in the encounter hostile to another
    // actor.  If no one is hostile to the player, they can choose to "pass" on their attack
    // turn.
    private boolean inCombat = false;

    public ActorEncounterModel(ActorEncounter encounter, ActorModel model) {
        super(encounter);
        this.model = model;
        this.actors = model.getActorsFor(getEncounter());
        
        combatants.addAll(actors);
        combatants.add(model.getPlayer());
        Collections.sort(combatants, new Comparator<Actor>() {
            @Override
            public int compare(Actor a1, Actor a2) {
                // Descending order by initiative
                return Integer.compare(a2.getInitiative(), a1.getInitiative());
            }
        });
    }
    
    /**
     * Invoke on self if target is not specified.  Some augmentations will also automatically
     * apply to a specific target or group of targets (like all) depending on range.
     */
    public void invoke(ActionAugmentation aug) {
        invoke(aug, aug.getOwner());
    }

    public void invoke(ActionAugmentation aug, Actor target) {
        if (canTakeAction(aug.getOwner())) {
            // Allow target to respond to the invocation
            boolean success = true;
            boolean hostileAction = false;
            switch (aug.getType()) {
                case ATTACK: // Playable to make hostile
                    success = target.handleAttack(aug);
                    hostileAction = true;
                    break;
                case DECEIVE: // Playable when not detected
                case EXECUTE: // Playable in encounter
                case DIALOGUE: // Playable in dialogue
                case COUNTER: // Playable when targeted
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
                    for (ActorEncounterListener listener : listeners) {
                        listener.effectApplied(result);
                    }
                }
                
                // TODO handle duration effects by keeping a list we apply at the end of the round
            }
            
            // Update combat state
            if (inCombat) {
                actions++;
                if (actions >= MAX_ACTIONS) {
                    nextCombatant();
                } else {
                    takeCombatTurn();
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
        return combatants.get(turn) == actor && actions < MAX_ACTIONS;
    }
    
    public void startCombat() {
        if (!inCombat) {
            inCombat = true;
            for (ActorEncounterListener listener : listeners) {
                listener.startedCombat();
            }
            Actor current = combatants.get(turn);
            for (ActorEncounterListener listener : listeners) {
                listener.combatTurnStarted(current);
            }
            takeCombatTurn();
        }
    }
    
    public void passCombat() {
        Actor current = combatants.get(turn);
        for (ActorEncounterListener listener : listeners) {
            listener.combatTurnPassed(current);
        }
        nextCombatant();
    }
    
    private void nextCombatant() {
        turn = (turn + 1) % combatants.size();
        actions = 0;
        
        Actor current = combatants.get(turn);
        for (ActorEncounterListener listener : listeners) {
            listener.combatTurnStarted(current);
        }
        
        takeCombatTurn();
    }
    
    private void takeCombatTurn() {
        combatants.get(turn).takeCombatTurn(this);
    }

    public boolean isInCombat() {
        return inCombat;
    }
    
    public void addListener(ActorEncounterListener listener) {
        listeners.add(listener);
    }
    
    public Player getPlayer() {
        return model.getPlayer();
    }
    
    public List<Npc> getActors() {
        return actors;
    }
    
    public ActorModel getActorModel() {
        return model;
    }
    
    public static interface ActorEncounterListener {
        void effectApplied(Result result);
        
        void startedCombat();
        
        void combatTurnStarted(Actor current);
        
        void combatTurnPassed(Actor current);
        
        void actorKilled(Actor actor);
        
        void actorTargeted(Actor actor);
    }
}
