package com.eldritch.invoken.actor.ai.btree;

import com.badlogic.gdx.ai.btree.branch.Selector;
import com.badlogic.gdx.ai.btree.branch.Sequence;
import com.badlogic.gdx.ai.btree.decorator.AlwaysSucceed;
import com.badlogic.gdx.ai.btree.decorator.Invert;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Npc;
import com.eldritch.invoken.location.Location;
import com.eldritch.invoken.util.GenericDialogue;

public class Patrol extends Selector<Npc> {
    public Patrol() {
        Sequence<Npc> guardSequence = new Sequence<Npc>();
        guardSequence.addChild(new IsGuard());
        guardSequence.addChild(new WatchForTrespassers());
        
        Sequence<Npc> threatenedSequence = new Sequence<Npc>();
        threatenedSequence.addChild(new IsThreatened());
        threatenedSequence.addChild(new RespondToThreat());
        
        Sequence<Npc> wanderSequence = new Sequence<Npc>();
        wanderSequence.addChild(new WatchForCrime());  // while wandering, check for crime
        wanderSequence.addChild(new AlwaysSucceed<Npc>(guardSequence));
        wanderSequence.addChild(new CanWander());
        wanderSequence.addChild(new Invert<Npc>(new IsTired()));
        wanderSequence.addChild(new LowerWeapon());
        wanderSequence.addChild(new Wander());
        
        addChild(wanderSequence);
        addChild(new Idle());
    }
    
    private static class WatchForCrime extends SuccessTask {
        @Override
        protected void doFor(Npc npc) {
            for (Agent neighbor : npc.getVisibleNeighbors()) {
                if (neighbor.isCommittingCrime()) {
                    // TODO: count this as a faction offense and confront them
                    // for now, just attack
                    // TODO: this should only accrue once for each crime
                    npc.changeRelation(neighbor, -10);
                    npc.announce(GenericDialogue.forCrime(npc, neighbor));
                }
            }
        }
    }
    
    private static class IsGuard extends BooleanTask {
        @Override
        protected boolean check(Npc npc) {
            return npc.isGuard() && !npc.getLocation().isTrespasser(npc);
        }
    }
    
    private static class WatchForTrespassers extends SuccessTask {
        @Override
        protected void doFor(Npc npc) {
            for (Agent neighbor : npc.getVisibleNeighbors()) {
                handleTrespasser(npc, neighbor);
            }
        }
    }
    
    private static class CanWander extends BooleanTask {
        @Override
        protected boolean check(Npc npc) {
            return !npc.inDialogue();
        }
    }
    
    private static class IsThreatened extends BooleanTask {
        @Override
        protected boolean check(Npc npc) {
            return npc.isGuard() && !npc.getLocation().isTrespasser(npc);
        }
    }
    
    private static class RespondToThreat extends SuccessTask {
        @Override
        protected void doFor(Npc npc) {
            for (Agent neighbor : npc.getVisibleNeighbors()) {
                handleTrespasser(npc, neighbor);
            }
        }
    }
    
    private static void handleTrespasser(Npc npc, Agent neighbor) {
        Location location = npc.getLocation();
        if (location.isTrespasser(neighbor)) {
            if (location.isOnFrontier(neighbor)) {
                npc.announce(GenericDialogue.forFrontier(npc, neighbor));
            } else {
                npc.changeRelation(neighbor, -10);
                npc.announce(GenericDialogue.forCrime(npc, neighbor));
            }
        }
    }
}
