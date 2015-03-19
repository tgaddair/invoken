package com.eldritch.invoken.actor.ai.btree;

import com.badlogic.gdx.ai.btree.branch.Selector;
import com.badlogic.gdx.ai.btree.branch.Sequence;
import com.eldritch.invoken.actor.type.Npc;
import com.eldritch.invoken.util.GenericDialogue;

public class Investigate extends Sequence<Npc> {
    public static final int INVESTIGATION_PENALTY = -5;
    
    public Investigate() {
        addChild(new IsSuspicious());

        // first we pursue the source of the suspicion
        Sequence<Npc> pursueSequence = new Sequence<Npc>();
        pursueSequence.addChild(new CanPursue());
        pursueSequence.addChild(new Pursue());

        // then we confront the instigator
        Sequence<Npc> confrontSequence = new Sequence<Npc>();
        confrontSequence.addChild(new HasCaughtInstigator());
        confrontSequence.addChild(new LowerRelation());

        // once we've confirmed the agent is suspicious, one of these actions will be taken
        Selector<Npc> selector = new Selector<Npc>();
        selector.addChild(pursueSequence);
        selector.addChild(confrontSequence);

        addChild(selector);
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

    private static class HasCaughtInstigator extends BooleanTask {
        @Override
        protected boolean check(Npc npc) {
            if (!npc.hasTarget()) {
                return false;
            }

            // we've certainly arrived at the suspicious source, but do we have line of sight?
            return npc.hasVisibilityTo(npc.getTarget());
        }
    }

    private static class LowerRelation extends SuccessTask {
        @Override
        protected void doFor(Npc npc) {
            npc.changeRelation(npc.getTarget(), INVESTIGATION_PENALTY);
            if (!npc.isEnemy(npc.getTarget())) {
                // express our disapproval
                npc.announce(GenericDialogue.forSuspiciousActivity(npc));
                npc.getThreat().setCalm();
            } else {
                // begin hostility
                npc.announce(GenericDialogue.forHostility(npc));
            }
        }
    }
}
