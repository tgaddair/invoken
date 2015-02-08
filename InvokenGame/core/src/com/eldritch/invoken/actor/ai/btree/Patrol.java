package com.eldritch.invoken.actor.ai.btree;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.branch.Selector;
import com.badlogic.gdx.ai.btree.branch.Sequence;
import com.eldritch.invoken.actor.type.Npc;

public class Patrol extends Selector<Npc> {
    public Patrol() {
//        addChild(new Follow());
        
        Sequence<Npc> wanderSequence = new Sequence<Npc>();
        wanderSequence.addChild(new CanWander());
        wanderSequence.addChild(new Wander());
        
        addChild(wanderSequence);
        addChild(new Idle());
    }
    
    private static class CanWander extends LeafTask<Npc> {
        @Override
        public void run(Npc entity) {
            if (check(entity)) {
                success();
            } else {
                fail();
            }
        }
        
        private boolean check(Npc npc) {
            return !npc.inDialogue() && !npc.getFatigue().isTired();
        }

        @Override
        protected Task<Npc> copyTo(Task<Npc> task) {
            return task;
        }
    }
}
