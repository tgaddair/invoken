package com.eldritch.invoken.actor.ai.btree;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.branch.Selector;
import com.badlogic.gdx.ai.btree.branch.Sequence;
import com.badlogic.gdx.ai.btree.decorator.AlwaysSucceed;
import com.badlogic.gdx.ai.btree.decorator.Invert;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.AgentInventory;
import com.eldritch.invoken.actor.aug.Augmentation;
import com.eldritch.invoken.actor.aug.Augmentation.Target;
import com.eldritch.invoken.actor.items.RangedWeapon;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Npc;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.util.GenericDialogue;
import com.eldritch.invoken.util.Heuristics;

public class Attack extends Sequence<Npc> {
    public Attack() {
        // if we can't select a target, then attacking fails
        addChild(new SelectBestTarget());
        // addChild(new EquipBestWeapon());

        Sequence<Npc> thrustSequence = new Sequence<>();
        thrustSequence.addChild(new ShouldThrust());
        thrustSequence.addChild(new Thrust());

        // if we have a chosen augmentation, then continue to hold aim until we
        // use it
        Sequence<Npc> useAugSequence = new Sequence<>();
        useAugSequence.addChild(new HasChosen());
        useAugSequence.addChild(new AcquireTarget());
        useAugSequence.addChild(new TakeAim());
        useAugSequence.addChild(new HasSights());
        useAugSequence.addChild(new AlwaysSucceed<>(thrustSequence));
        useAugSequence.addChild(new UseAugmentation());
        useAugSequence.addChild(new LowerAim());

        Sequence<Npc> chooseAugSequence = new Sequence<>();
        chooseAugSequence.addChild(new ChooseWeapon());
        chooseAugSequence.addChild(new ChooseAugmentation());
        chooseAugSequence.addChild(new TakeAim());

        // attempt to use the augmentation, if we cannot use the augmentation
        // then we
        // fail over to evading the target
        Selector<Npc> augSelector = new Selector<>();
        augSelector.addChild(useAugSequence);
        augSelector.addChild(chooseAugSequence);
        augSelector.addChild(new AlwaysSucceed<>(new LowerAim())); // failed to
                                                                   // choose
                                                                   // aug, so
                                                                   // lower
                                                                   // our aim

        Sequence<Npc> dodgeSequence = new Sequence<>();
        dodgeSequence.addChild(new ShouldDodge());
        dodgeSequence.addChild(new LowerAim());
        dodgeSequence.addChild(new Dodge());

        // hide if we have line of sight to our last seen, otherwise we idle in
        // defensive posture
        Sequence<Npc> hideSequence = new Sequence<>();
        hideSequence.addChild(new DesiresCover());
        hideSequence.addChild(new IsIntimidated());
        hideSequence.addChild(new Invert<>(new IsTrapped()));
        hideSequence.addChild(new Invert<>(new HasCover()));
        hideSequence.addChild(new Reload());
        hideSequence.addChild(new SeekCover());

        Sequence<Npc> pursueSequence = new Sequence<>();
        pursueSequence.addChild(new HasTarget());
        pursueSequence.addChild(new Pursue());

        Selector<Npc> evasionSelector = new Selector<>();
        evasionSelector.addChild(dodgeSequence);
        evasionSelector.addChild(hideSequence);
        evasionSelector.addChild(pursueSequence);
        // selector.addChild(suppressSequence);

        addChild(new AlwaysSucceed<>(evasionSelector));
        addChild(augSelector);
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

    private static class ChooseWeapon extends SuccessTask {
        @Override
        public void doFor(Npc npc) {
            AgentInventory inv = npc.getInventory();
            if (inv.hasRangedWeapon()) {
                if (inv.getAmmunitionCount() <= 0) {
                    inv.unequip(inv.getRangedWeapon());
                }
            }
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

            Level level = npc.getLocation();
            if (!npc.canTarget(target, level)) {
                // can't attack invalid targets
                return false;
            }

            Target tmpTarget = new Target();
            if (!npc.hasPendingAction() && !npc.actionInProgress()) {
                // choose the aug with the highest situational quality score
                Augmentation chosen = null;

                // sometimes we don't want to choose an aug
                float bestQuality = (float) (Math.random() * Heuristics.getDesperation(npc));
                for (Augmentation aug : npc.getInfo().getAugmentations().getAugmentations()) {
                    if (aug.hasEnergy(npc)
                            && aug.isValidWithAiming(npc,
                                    aug.getBestTarget(npc, npc.getTarget(), tmpTarget))) {
                        float quality = aug.quality(npc, npc.getTarget(), level);
                        if (quality > bestQuality) {
                            chosen = aug;
                            bestQuality = quality;
                            npc.getTactics().setTarget(tmpTarget);
                        }
                    }
                }

                // TODO: incorporate other signals besides "quality" into our
                // decision-making
                // process. specifically, think about choices in terms of "risk"
                // and "reward" and
                // how an individual's preferences change based on their
                // attributes

                // if an aug was chosen, then go ahead and use it
                if (chosen != null) {
                    npc.getInfo().getAugmentations().prepare(chosen);
                }
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

    private static class AcquireTarget extends SuccessTask {
        @Override
        public void doFor(Npc npc) {
            Target target = npc.getTactics().getTarget();
            if (target.isValid() && target.isAgent()) {
                npc.setTarget(target.getAgent());
            }
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

            Target target = npc.getTactics().getTarget();
            if (target.isAgent()) {
                return npc.isAimingAt(target.getAgent());
            } else {
                return npc.hasLineOfSight(target.getLocation());
            }
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
            if (!npc.hasTarget()) {
                return false;
            }

            // ranged attackers desire cover
            return npc.isCloaked() || npc.getInventory().hasRangedWeapon()
                    || !npc.getInventory().hasMeleeWeapon();
        }
    }

    private static class HasCover extends BooleanTask {
        @Override
        protected boolean check(Npc npc) {
            return !npc.getLocation().hasLineOfSight(npc, npc.getPosition(),
                    npc.getLastSeen().getPosition())
                    && npc.getCover() != null
                    && npc.getCover().getPosition().dst2(npc.getPosition()) < 1;
        }
    }

    private static class Reload extends SuccessTask {
        @Override
        public void doFor(Npc npc) {
            AgentInventory inv = npc.getInventory();
            if (inv.hasRangedWeapon()) {
                if (inv.getClip() == 0 && inv.getAmmunitionCount() > 0 && !inv.isReloading()) {
                    inv.reloadWeapon();
                    npc.announce(GenericDialogue.reloading(npc));
                }
            }
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

    private static class ShouldDodge extends BooleanTask {
        @Override
        protected boolean check(Npc npc) {
            if (!npc.canDodge()) {
                return false;
            }

            // when our target is aiming at us, then dodge with some probability
            float stealth = npc.getInfo().getStealthModifier();
            if (stealth > 0.7) {
                float danger = getDanger(npc);
                if (danger > 0) {
                    // average about a second of waiting before dodging
                    // if this routine is called once every STEP seconds, then
                    // we want to act after
                    // 1 / STEP invocations
                    return Math.random() * danger * stealth > Npc.STEP;
                }
            }
            return false;
        }

        private float getDanger(Npc npc) {
            if (npc.hasTarget() && npc.getTarget().isAimingAt(npc)) {
                Agent target = npc.getTarget();

                float threat = 1.0f;
                if (target.getInventory().hasRangedWeapon()) {
                    float dst2 = npc.dst2(target);
                    RangedWeapon weapon = target.getInventory().getRangedWeapon();
                    float ideal2 = weapon.getIdealDistance();
                    threat = Heuristics.distanceScore(dst2, ideal2);
                }

                float danger = threat * Heuristics.getDesperation(npc);
                return danger;
            }

            return 0;
        }
    }

    private static class Dodge extends SuccessTask {
        private final Vector2 direction = new Vector2();

        @Override
        public void doFor(Npc npc) {
            Agent target = npc.getTarget();
            direction.set(target.getPosition()).sub(npc.getPosition()).nor();

            if (!npc.getInventory().hasMeleeWeapon() && npc.dst2(npc.getTarget()) < 3) {
                // dodge back
                direction.scl(-1);
            } else {
                // randomly dodge left or right
                direction.rotate90((int) Math.signum(Math.random() - 0.5));
            }

            npc.dodge(direction);
        }
    }

    private static class ShouldThrust extends BooleanTask {
        @Override
        protected boolean check(Npc npc) {
            if (!npc.getInventory().hasMeleeWeapon() || !npc.hasTarget()) {
                return false;
            }
            return npc.dst2(npc.getTarget()) < 3;
        }
    }

    private static class Thrust extends SuccessTask {
        @Override
        public void doFor(Npc npc) {
            npc.thrust(npc.getTarget());
        }
    }

    private static class HasTarget extends BooleanTask {
        @Override
        protected boolean check(Npc npc) {
            return npc.hasTarget();
        }
    }
}
