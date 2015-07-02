package com.eldritch.invoken.actor.ai.planning;

import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.branch.Sequence;
import com.badlogic.gdx.ai.btree.decorator.Invert;
import com.eldritch.invoken.actor.PreparedAugmentations;
import com.eldritch.invoken.actor.ai.btree.BooleanTask;
import com.eldritch.invoken.actor.ai.btree.IsTired;
import com.eldritch.invoken.actor.ai.btree.LowerWeapon;
import com.eldritch.invoken.actor.ai.btree.SuccessTask;
import com.eldritch.invoken.actor.ai.btree.Wander;
import com.eldritch.invoken.actor.ai.planning.Desire.AbstractDesire;
import com.eldritch.invoken.actor.aug.Augmentation;
import com.eldritch.invoken.actor.aug.Augmentation.Target;
import com.eldritch.invoken.actor.type.Npc;
import com.eldritch.invoken.location.Level;

public class Buff extends AbstractDesire {
    private final BehaviorTree<Npc> tree;
    private boolean active = false;
    private boolean done = false;

    public Buff(Npc owner) {
        super(owner);
        tree = new BehaviorTree<Npc>(createBehavior(), owner);
    }

    @Override
    public boolean act() {
        // active will be flipped to true if all the preconditions on the tree
        // are satisfied
        active = false;
        tree.step();
        done = true;
        return active;
    }

    @Override
    public float getValue() {
        return !done ? 1f : 0f;
    }

    private Task<Npc> createBehavior() {
        Sequence<Npc> sequence = new Sequence<>();
        sequence.addChild(new BuffTask());
        return sequence;
    }

    private static class BuffTask extends SuccessTask {
        @Override
        protected void doFor(Npc npc) {
            Target tmpTarget = new Target();
            Level level = npc.getLocation();
            PreparedAugmentations prepared = npc.getInfo().getAugmentations();
            for (Augmentation aug : prepared.getAugmentations()) {
                if (aug.hasEnergy(npc)
                        && aug.isValidWithAiming(npc, aug.getBestTarget(npc, npc, tmpTarget))) {
                    float quality = aug.quality(npc, npc, level);
                    if (quality > 0) {
                        prepared.prepare(aug);
                        prepared.use(aug, tmpTarget, true);
                    }
                }
            }
        }
    }
}
