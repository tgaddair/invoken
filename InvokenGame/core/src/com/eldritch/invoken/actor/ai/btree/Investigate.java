package com.eldritch.invoken.actor.ai.btree;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.branch.Selector;
import com.badlogic.gdx.ai.btree.branch.Sequence;
import com.eldritch.invoken.actor.type.Npc;

public class Investigate extends Sequence<Npc> {
    public Investigate() {
        Sequence<Npc> explorationSequence = new Sequence<Npc>();
        explorationSequence.addChild(new CanExplore());
        explorationSequence.addChild(new Wander());
        explorationSequence.addChild(new Explore());
        
        Sequence<Npc> pursueSequence = new Sequence<Npc>();
        pursueSequence.addChild(new CanPursue());
        pursueSequence.addChild(new Pursue());

        // once we've confirmed the agent is suspicious, one of these actions will be taken
        Selector<Npc> selector = new Selector<Npc>();
        selector.addChild(explorationSequence);
        selector.addChild(pursueSequence);
        selector.addChild(new ResetExploration());
        
        addChild(new IsSuspicious());
        addChild(selector);
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

    private static class IsSuspicious extends BooleanTask {
        @Override
        protected boolean check(Npc npc) {
            return npc.isSuspicious();
        }
    }

    private static class CanPursue extends BooleanTask {
        @Override
        protected boolean check(Npc npc) {
            return npc.getPosition().dst2(npc.getLastSeen().getPosition()) > 5;
        }
    }
    
    private static class ResetExploration extends LeafTask<Npc> {
        @Override
        public void run(Npc npc) {
            npc.getExploration().setReady();
            success();
        }

        @Override
        protected Task<Npc> copyTo(Task<Npc> task) {
            return task;
        }
    }
}
