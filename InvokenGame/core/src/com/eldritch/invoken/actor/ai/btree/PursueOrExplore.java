package com.eldritch.invoken.actor.ai.btree;

import com.badlogic.gdx.ai.btree.branch.Selector;
import com.badlogic.gdx.ai.btree.branch.Sequence;
import com.eldritch.invoken.actor.type.Npc;

public class PursueOrExplore extends Selector<Npc> {
    private static final float EXPLORATION_RANGE = 10f;

    public PursueOrExplore(Sequence<Npc> pursueSequence) {
        Sequence<Npc> explorationSequence = new Sequence<Npc>();
        explorationSequence.addChild(new CanExplore());
        explorationSequence.addChild(new Wander());

        addChild(pursueSequence);
        addChild(explorationSequence);
        addChild(new ResetArrived());
    }

    private static class CanExplore extends BooleanTask {
        @Override
        protected boolean check(Npc npc) {
            float r = EXPLORATION_RANGE;
            return npc.getPosition().dst2(npc.getLastSeen().getPosition()) < r * r;
        }
    }

    private static class ResetArrived extends SuccessTask {
        @Override
        protected void doFor(Npc npc) {
            npc.getLastSeen().setArrived(false);
        }
    }
}
