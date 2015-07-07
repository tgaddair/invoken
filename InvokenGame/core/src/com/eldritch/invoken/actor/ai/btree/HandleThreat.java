package com.eldritch.invoken.actor.ai.btree;

import com.badlogic.gdx.ai.btree.branch.Selector;
import com.badlogic.gdx.ai.btree.branch.Sequence;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Npc;
import com.eldritch.invoken.util.GenericDialogue;

public class HandleThreat extends Selector<Npc> {
    public static final float DURESS_PENALTY = -0.1f;

    public HandleThreat() {
        Sequence<Npc> calmSequence = new Sequence<Npc>();
        calmSequence.addChild(new IsCalm());
        calmSequence.addChild(new RespondToThreat());
        calmSequence.addChild(new SetLastTask("RespondToThreat"));

        // what to do when actively under duress
        Sequence<Npc> duressSequence = new Sequence<Npc>();
        duressSequence.addChild(new IsUnderDuress());
        duressSequence.addChild(new SetLastTask("Duress"));

        // if the threatener is still threatening us then lower our relation to them
        Selector<Npc> duressSelector = new Selector<Npc>();
        duressSelector.addChild(Tasks.sequence(new TargetThreatening(), new LowerRelation(),
                new Idle()));
        duressSelector.addChild(new CalmDown());  // otherwise, calm down
        duressSequence.addChild(duressSelector);

        Selector<Npc> selector = new Selector<Npc>();
        selector.addChild(calmSequence);
        selector.addChild(duressSequence);
        addChild(selector);
    }

    private static class IsCalm extends BooleanTask {
        @Override
        protected boolean check(Npc npc) {
            return npc.getThreat().isCalm();
        }
    }

    private static class RespondToThreat extends BooleanTask {
        @Override
        protected boolean check(Npc npc) {
            for (Agent neighbor : npc.getVisibleNeighbors()) {
                if (neighbor.getInventory().hasRangedWeapon() && neighbor.hasSentryReady()
                        && neighbor.isAimingAt(npc)) {
                    // we're being threatened
                    npc.getThreat().setDuress(neighbor);
                    npc.setTarget(neighbor);
                    npc.announce(GenericDialogue.forDuress(npc, neighbor));
                    return true;
                }
            }
            return false;
        }
    }

    private static class IsUnderDuress extends BooleanTask {
        @Override
        protected boolean check(Npc npc) {
            return npc.getThreat().isUnderDuress();
        }
    }

    private static class TargetThreatening extends BooleanTask {
        @Override
        protected boolean check(Npc npc) {
            Agent target = npc.getThreat().getThreatener();
            if (!target.isAlive() || !npc.hasVisibilityTo(target)) {
                return false;
            }

            // target must put down weapon before we're calm again
            return target.getInventory().hasRangedWeapon() && target.hasSentryReady();
        }
    }

    private static class LowerRelation extends SuccessTask {
        @Override
        protected void doFor(Npc npc) {
            Agent target = npc.getThreat().getThreatener();
            npc.changeRelationScaled(target, DURESS_PENALTY, 0);
            if (npc.isEnemy(target)) {
                // begin hostility
                npc.announce(GenericDialogue.forHostility(npc, target));
            }
        }
    }

    private static class CalmDown extends SuccessTask {
        @Override
        protected void doFor(Npc npc) {
            Agent target = npc.getThreat().getThreatener();
            npc.getThreat().setCalm();
            if (!target.isAlive() || !npc.hasVisibilityTo(target)) {
                // nothing else to do
                return;
            }
            
            if (!npc.getThreat().hasEnemy(target)) {
                npc.announce(GenericDialogue.thank(npc, target));
            }
        }
    }
}
