package com.eldritch.invoken.actor.ai.btree;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.branch.Selector;
import com.badlogic.gdx.ai.btree.branch.Sequence;
import com.eldritch.invoken.actor.type.Npc;

public class PursueOrExplore extends Selector<Npc> {
    public PursueOrExplore(Sequence<Npc> pursueSequence) {
        Sequence<Npc> explorationSequence = new Sequence<Npc>();
        explorationSequence.addChild(new CanExplore());
        explorationSequence.addChild(new Wander());
        explorationSequence.addChild(new Explore());
        
        addChild(explorationSequence);
        addChild(pursueSequence);
        addChild(new ResetExploration());
    }
    
    private static class CanExplore extends BooleanTask {
        @Override
        protected boolean check(Npc npc) {
            return !npc.getExploration().isExpended();
        }
    }
    
    private static class Explore extends LeafTask<Npc> {
        @Override
        public void run(Npc npc) {
            npc.getExploration().use(Npc.STEP);
            npc.setTask(getClass().getSimpleName());
            success();
        }

        @Override
        protected Task<Npc> copyTo(Task<Npc> task) {
            return task;
        }
    }
    
    private static class ResetExploration extends LeafTask<Npc> {
        @Override
        public void run(Npc npc) {
            System.out.println("reset exploration");
            npc.getExploration().setReady();
            success();
        }

        @Override
        protected Task<Npc> copyTo(Task<Npc> task) {
            return task;
        }
    }
}
