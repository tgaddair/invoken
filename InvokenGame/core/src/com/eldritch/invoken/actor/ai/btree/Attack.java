package com.eldritch.invoken.actor.ai.btree;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.branch.Selector;
import com.badlogic.gdx.ai.btree.branch.Sequence;
import com.badlogic.gdx.ai.btree.decorator.AlwaysFail;
import com.badlogic.gdx.ai.btree.decorator.AlwaysSucceed;
import com.badlogic.gdx.ai.btree.decorator.Invert;
import com.eldritch.invoken.actor.aug.Augmentation;
import com.eldritch.invoken.actor.aug.Augmentation.Target;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Npc;
import com.eldritch.invoken.encounter.Location;

public class Attack extends Sequence<Npc> {
    public Attack() {
        // if we can't select a target, then attacking fails
        addChild(new SelectBestTarget());

        // if we have a chosen augmentation, then continue to hold aim until we use it
        Sequence<Npc> useAugSequence = new Sequence<Npc>();
        useAugSequence.addChild(new HasChosen());
        useAugSequence.addChild(new HasSights());
        useAugSequence.addChild(new UseAugmentation());
        useAugSequence.addChild(new LowerAim());

        Sequence<Npc> chooseAugSequence = new Sequence<Npc>();
        chooseAugSequence.addChild(new ChooseAugmentation());
        chooseAugSequence.addChild(new TakeAim());

        // attempt to use the augmentation, if we cannot use the augmentation then we
        // fail over to evading the target
        Selector<Npc> augSelector = new Selector<Npc>();
        augSelector.addChild(useAugSequence);
        augSelector.addChild(chooseAugSequence);
        augSelector.addChild(new AlwaysFail<Npc>(new LowerAim())); // failed to choose aug, so lower
                                                                   // our aim

        // hide if we have line of sight to our last seen, otherwise we idle in defensive posture
        Sequence<Npc> hideSequence = new Sequence<Npc>();
        hideSequence.addChild(new DesiresCover());
        hideSequence.addChild(new IsIntimidated());
        hideSequence.addChild(new Invert<Npc>(new HasCover()));
        hideSequence.addChild(new SeekCover());

        Selector<Npc> selector = new Selector<Npc>();
        selector.addChild(augSelector);
        selector.addChild(hideSequence);
        // selector.addChild(suppressSequence);

        addChild(selector);
    }

    @Override
    protected Task<Npc> copyTo(Task<Npc> task) {
        return task;
    }

    private static class SelectBestTarget extends LeafTask<Npc> {
        private final List<Agent> targets = new ArrayList<Agent>();

        @Override
        public void run(Npc entity) {
            fillTargets(entity);
            Agent target = selectBestTarget(entity);
            if (target != null) {
                entity.setTarget(target);
            }

            if (entity.hasTarget()) {
                success();
            } else {
                fail();
            }
        }

        protected final void fillTargets(Npc entity) {
            targets.clear();
            entity.getBehavior().getAssaultTargets(entity.getVisibleNeighbors(), targets);
        }

        protected final Agent selectBestTarget(Npc entity) {
            // get one of our enemies
            Agent current = null;
            float bestDistance = Float.MAX_VALUE;
            for (Agent agent : targets) {
                if (!agent.isAlive()) {
                    // no point in attacking a dead enemy
                    continue;
                }

                float distance = entity.dst2(agent);
                if (current == null || distance < bestDistance) {
                    // attack the closer enemy
                    current = agent;
                    bestDistance = distance;
                }
            }
            return current;
        }

        @Override
        protected Task<Npc> copyTo(Task<Npc> task) {
            return task;
        }
    }

    private static class HasChosen extends BooleanTask {
        @Override
        protected boolean check(Npc npc) {
            return npc.hasChosen();
        }
    }

    private static class ChooseAugmentation extends LeafTask<Npc> {
        @Override
        public void run(Npc entity) {
            if (canUse(entity)) {
                success();
            } else {
                fail();
            }
        }

        private boolean canUse(Npc npc) {
            Agent target = npc.getTarget();
            if (target == null) {
                // nothing to use on
                return false;
            }

            Location location = npc.getLocation();
            if (!npc.canTarget(target, location)) {
                // can't attack invalid targets
                return false;
            }

            Target tmpTarget = new Target();
            if (!npc.hasPendingAction() && !npc.actionInProgress()) {
                // choose the aug with the highest situational quality score
                Augmentation chosen = null;
                float bestQuality = 0; // never choose an aug with quality <= 0
                for (Augmentation aug : npc.getInfo().getAugmentations().getAugmentations()) {
                    if (aug.hasEnergy(npc)
                            && aug.isValidWithAiming(npc, aug.getBestTarget(npc, npc.getTarget(),
                                    tmpTarget))) {
                        float quality = aug.quality(npc, npc.getTarget(), location);
                        if (quality > bestQuality) {
                            chosen = aug;
                            bestQuality = quality;
                            npc.getTactics().setTarget(tmpTarget);
                        }
                    }
                }

                // if an aug was chosen, then go ahead and use it
                npc.setChosen(chosen);
                return chosen != null;
            }
            return false;
        }

        @Override
        protected Task<Npc> copyTo(Task<Npc> task) {
            return task;
        }
    }

    private static class TakeAim extends SuccessTask {
        @Override
        public void doFor(Npc entity) {
            if (!entity.hasChosen() || !entity.getChosen().isAimed()) {
                // no need to take aim
                return;
            }
            entity.setAiming(true);
        }
    }

    private static class HasSights extends BooleanTask {
        @Override
        protected boolean check(Npc npc) {
            if (npc.hasChosen() && !npc.getChosen().isAimed()) {
                // don't need to aim this aug, so we have sights
                return true;
            }
            return npc.hasSights();
        }
    }

    private static class UseAugmentation extends LeafTask<Npc> {
        @Override
        public void run(Npc entity) {
            if (use(entity)) {
                entity.setTask(getClass().getSimpleName());
                success();
            } else {
                fail();
            }
        }

        private boolean use(Npc npc) {
            Augmentation chosen = npc.getChosen();
            npc.getInfo().getAugmentations().prepare(chosen);
            npc.getInfo().getAugmentations().use(chosen, npc.getTactics().getTarget());
            npc.setChosen(null);
            return true;
        }

        @Override
        protected Task<Npc> copyTo(Task<Npc> task) {
            return task;
        }
    }

    private static class DesiresCover extends BooleanTask {
        @Override
        protected boolean check(Npc npc) {
            return npc.getInventory().hasRangedWeapon() && npc.hasTarget();
        }
    }

    private static class HasCover extends BooleanTask {
        @Override
        protected boolean check(Npc npc) {
            return !npc.getLocation().hasLineOfSight(npc.getPosition(),
                    npc.getLastSeen().getPosition())
                    && npc.getCover() != null
                    && npc.getCover().getPosition().dst2(npc.getPosition()) < 1;
        }
    }

    private static class HasLineOfSight extends BooleanTask {
        @Override
        protected boolean check(Npc npc) {
            return npc.hasTarget() && npc.hasLineOfSight(npc.getTarget());
        }
    }

    private static class HasLastSeen extends BooleanTask {
        @Override
        protected boolean check(Npc npc) {
            return npc.hasTarget() && npc.hasLineOfSight(npc.getLastSeen().getPosition());
        }
    }

    private static class LowerAim extends SuccessTask {
        @Override
        public void doFor(Npc entity) {
            entity.setAiming(false);
        }
    }
}
