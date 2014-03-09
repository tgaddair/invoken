package com.eldritch.scifirpg.game.model;

import java.util.List;
import java.util.Set;

import com.eldritch.scifirpg.game.model.actor.Actor;
import com.eldritch.scifirpg.game.util.Result;
import com.eldritch.scifirpg.proto.Outcomes.Outcome;

public interface EncounterListener {
    void outcomesApplied(List<Outcome> outcomes);
    
    public static interface ActorEncounterListener extends EncounterListener {
        void effectApplied(Result result);
        
        void startedCombat();
        
        void endedCombat();
        
        void combatTurnStarted(Actor current);
        
        void combatTurnPassed(Actor current);
        
        void actorKilled(Actor actor);
        
        void actorTargeted(Actor actor);
        
        void actionUsed(ActiveAugmentation action);
        
        void actionsDrawn(Actor actor, Set<ActiveAugmentation> actions);
        
        void canContinue(boolean can);
        
        void playerKilled();
    }
}
