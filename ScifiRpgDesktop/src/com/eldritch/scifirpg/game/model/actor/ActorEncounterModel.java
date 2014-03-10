package com.eldritch.scifirpg.game.model.actor;

import java.util.ArrayList;
import java.util.List;

import com.eldritch.scifirpg.game.model.ActiveAugmentation;
import com.eldritch.scifirpg.game.model.EncounterListener.ActorEncounterListener;
import com.eldritch.scifirpg.game.model.EncounterModel;
import com.eldritch.scifirpg.game.model.GameState;
import com.eldritch.scifirpg.game.util.Result;

/**
 * Handles the internal state of a single ActorEncounter. Makes requests to the
 * global ActorModel for data and updates the ActorModel's persistent state
 * (who's alive, etc.).
 * 
 */
public class ActorEncounterModel extends EncounterModel<ActorEncounter, ActorEncounterListener> {
    private final ActorModel model;
    private final ActionModel actionModel;
    private final List<Npc> npcs;

    public ActorEncounterModel(ActorEncounter encounter, GameState state) {
        super(encounter, state);
        this.model = state.getActorModel();
        this.npcs = model.getActorsFor(getEncounter());
        this.actionModel = new ActionModel(this);
    }
    
    public ActorState getState(Actor actor) {
        return actionModel.getState(actor);
    }
    
    public void passCombat(Actor actor) {
        actionModel.passCombat(getState(actor));
    }
    
    public void takeAction(ActiveAugmentation aug, Actor actor) {
        Action action = new Action(aug, actionModel.getState(actor));
        actionModel.takeAction(action);
    }
    
    public void takeAction(ActiveAugmentation aug, Actor actor, ActorState target) {
        Action action = new Action(aug, actionModel.getState(actor), target);
        actionModel.takeAction(action);
    }
    
    private boolean isAlive(Actor actor) {
        return actionModel.getState(actor).isAlive();
    }
    
    @Override
    public boolean canContinue() {
        for (Npc actor : npcs) {
            if (isAlive(actor)) {
                if (getState(actor).hasEnemy(getState(model.getPlayer()))) {
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
    
    public boolean checkHostility() {
        boolean hasHostile = false;
        for (Npc actor : npcs) {
            if (isAlive(actor)) {
                if (getState(actor).hasEnemy()) {
                    hasHostile = true;
                }
            } else {
                onNpcKilled(actor);
            }
        }
        
        // Also update continue state
        boolean continuable = canContinue();
        for (ActorEncounterListener listener : getListeners()) {
            listener.canContinue(continuable);
        }
        
        return hasHostile;
    }
    
    public void checkActorsAlive() {
        for (Npc actor : npcs) {
            if (!isAlive(actor)) {
                onNpcKilled(actor);
            }
        }
        if (!isAlive(model.getPlayer())) {
            onPlayerKilled();
        }
    }
    
    public void onNpcKilled(Npc actor) {
        model.markDead(actor.getId());
        for (ActorEncounterListener listener : getListeners()) {
            listener.actorKilled(actor);
        }
        
        // Apply outcomes on NPC death
        applyOutcomes(actor.getDeathOutcomes());
    }
    
    public void onPlayerKilled() {
        for (ActorEncounterListener listener : getListeners()) {
            listener.playerKilled();
        }
    }
    
    public void onCombatEnded() {
        for (ActorEncounterListener listener : getListeners()) {
            listener.endedCombat();
        }
    }
    
    public void onCombatStarted() {
        for (ActorEncounterListener listener : getListeners()) {
            listener.startedCombat();
        }
    }
    
    public void onCombatTurnStarted(Actor actor) {
        for (ActorEncounterListener listener : getListeners()) {
            listener.combatTurnStarted(actor);
        }
    }
    
    public void onActionRequested(Actor actor) {
        for (ActorEncounterListener listener : getListeners()) {
            listener.actionRequested(actor);
        }
    }
    
    public void onCombatPassed(Actor actor) {
        for (ActorEncounterListener listener : getListeners()) {
            listener.combatTurnPassed(actor);
        }
    }
    
    public void onResults(List<Result> results) {
        for (ActorEncounterListener listener : getListeners()) {
            listener.handleResults(results);
        }
    }
    
    public void onCombatTurnEnded(Actor actor) {
        for (ActorEncounterListener listener : getListeners()) {
            listener.combatTurnEnded(actor);
        }
    }
    
    public Player getPlayer() {
        return model.getPlayer();
    }
    
    public List<Npc> getNpcs() {
        return npcs;
    }
    
    public List<Actor> getActors() {
        List<Actor> actors = new ArrayList<>();
        actors.addAll(npcs);
        actors.add(model.getPlayer());
        return actors;
    }
    
    public ActorModel getActorModel() {
        return model;
    }
}
