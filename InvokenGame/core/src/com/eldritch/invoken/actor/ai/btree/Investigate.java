package com.eldritch.invoken.actor.ai.btree;

import com.badlogic.gdx.ai.btree.branch.Selector;
import com.badlogic.gdx.ai.btree.branch.Sequence;
import com.badlogic.gdx.ai.btree.decorator.AlwaysFail;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Npc;
import com.eldritch.invoken.util.GenericDialogue;

public class Investigate extends Sequence<Npc> {
    public static final int INVESTIGATION_PENALTY = -5;
    private static final float RANGE2 = 3f * 3f;
    
    public Investigate() {
        addChild(new IsSuspicious());
        addChild(new SetLastTask("Investigate"));

        // first we pursue the source of the suspicion
        Sequence<Npc> pursueSequence = new Sequence<>();
        pursueSequence.addChild(new CanPursue());
        pursueSequence.addChild(new Pursue());
        
        // perhaps we found something alerting
        Sequence<Npc> alertSequence = new Sequence<>();
        alertSequence.addChild(new FoundDeadAlly());
        alertSequence.addChild(new Alert());

        // then we confront the instigator
        Sequence<Npc> confrontSequence = new Sequence<>();
        confrontSequence.addChild(new HasCaughtInstigator());
        confrontSequence.addChild(new LowerRelation());

        // once we've confirmed the agent is suspicious, one of these actions will be taken
        Selector<Npc> selector = new Selector<>();
        selector.addChild(alertSequence);
        selector.addChild(pursueSequence);
        selector.addChild(confrontSequence);
        selector.addChild(new AlwaysFail<>(new DropTarget()));

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
            return npc.dst2(npc.getLastSeen()) > RANGE2;
        }
    }

    private static class HasCaughtInstigator extends BooleanTask {
        @Override
        protected boolean check(Npc npc) {
            if (!npc.hasTarget() || !npc.getTarget().isAlive()) {
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
//                npc.announce(GenericDialogue.forSuspiciousActivity(npc, npc.getTarget()));
                npc.getThreat().setCalm();
            } else {
                // begin hostility
                npc.announce(GenericDialogue.forHostility(npc, npc.getTarget()));
            }
        }
    }
    
    private static class FoundDeadAlly extends BooleanTask {
        @Override
        protected boolean check(Npc npc) {
            for (Agent neighbor : npc.getVisibleNeighbors()) {
                if (!npc.isAlive() && npc.isAlly(neighbor)) {
                    return true;
                }
            }
            return false;
        }
    }

    private static class Alert extends SuccessTask {
        @Override
        protected void doFor(Npc npc) {
            npc.announce(GenericDialogue.forDeadAlly(npc));
            npc.getThreat().setAlerted();
        }
    }
    
    private static class DropTarget extends SuccessTask {
        @Override
        protected void doFor(Npc npc) {
            npc.setTarget(null);
        }
    }
}
