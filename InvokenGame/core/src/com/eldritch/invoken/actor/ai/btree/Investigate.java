package com.eldritch.invoken.actor.ai.btree;

import com.badlogic.gdx.ai.btree.branch.Sequence;
import com.eldritch.invoken.actor.type.Npc;

public class Investigate extends Sequence<Npc> {
    public Investigate() {
        Sequence<Npc> pursueSequence = new Sequence<Npc>();
        pursueSequence.addChild(new CanPursue());
        pursueSequence.addChild(new Pursue());

        // once we've confirmed the agent is suspicious, one of these actions will be taken
        addChild(new IsSuspicious());
        addChild(new PursueOrExplore(pursueSequence));
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
            return !npc.getLastSeen().hasArrived();
        }
    }
}
