package com.eldritch.invoken.actor.ai.btree;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.branch.Sequence;
import com.eldritch.invoken.actor.type.Npc;

public class Investigate extends Sequence<Npc> {
    public Investigate() {
        addChild(new IsAlerted());
        addChild(new Pursue());
    }
    
    private static class IsAlerted extends LeafTask<Npc> {
        @Override
        public void run(Npc entity) {
            if (check(entity)) {
                success();
            } else {
                fail();
            }
        }
        
        private boolean check(Npc npc) {
            return npc.isAlerted();
        }

        @Override
        protected Task<Npc> copyTo(Task<Npc> task) {
            return task;
        }
    }
}
